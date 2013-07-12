import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import javax.sql.DataSource;

import jdbchelper.JdbcHelper;
import jdbchelper.QueryResult;
import jdbchelper.SimpleDataSource;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;

public class Hacienda {
	final String detalleUrl = "http://www.buenosaires.gob.ar/areas/hacienda/compras/consulta/popup_detalle.php?popup_modulo=popup_altas_detalle&estado=6&idlicitacion=%d&tipo=adjudicacion";
	final JdbcHelper jdbc;

	static final String FILES_DIR = "down/";

	public Hacienda() {
		DataSource dataSource = new SimpleDataSource(
				"org.hsqldb.jdbc.JDBCDriver", "jdbc:hsqldb:file:db/testdb",
				"sa", "");
		jdbc = new JdbcHelper(dataSource);
	}

	public void parse() {
		try {
			for (int i = 0; i < 46000; i += 100) {
				parseHtml(i);
			}
		} catch (Exception e) {
			if (jdbc != null) {
				jdbc.execute("shutdown");
			}

			e.printStackTrace();
		}
	}

	private void parseHtml(int num) throws Exception {
		Document doc = Jsoup.parse(new File("htmls/" + num + ".html"),
				"iso-8859-1");

		Elements trs = doc.select("table.lista > tbody > tr");

		for (Element tr : trs) {
			Elements spans = tr.select("td > span");

			String contratacion = spans.get(0).text().trim();
			String actuacion = spans.get(1).text().trim();
			String rubro = spans.get(2).text().trim();
			String fecha = spans.get(3).text().trim();
			String rSolicitante = spans.get(4).text().trim();
			String rLicitante = spans.get(5).text().trim();
			String estado = spans.get(6).text().trim();

			String href = spans.get(7).getElementsByTag("a").first()
					.attr("href");
			int idlicitacion = Ints.tryParse(href.substring(
					href.indexOf("idlicitacion=") + "idlicitacion=".length())
					.trim());

			QueryResult res = jdbc.query(
					"select * from licitacion where id = ?", idlicitacion);

			boolean existeRegistro = res.next();
			boolean existeDetalle = existeRegistro
					&& res.getString("empresa") != null;
			boolean existeArchivo = existeRegistro
					&& res.getString("archivo") != null;
			String link = existeDetalle ? res.getString("link") : null;

			// id, contratacion, actuacion, rubro, fecha, solicitante,
			// licitante, estado, link, archivo, observaciones,
			// empresa

			if (!existeRegistro) {
				jdbc.execute(
						"insert into licitacion (id, contratacion, actuacion, rubro, fecha, solicitante, licitante, estado) "
								+ "values (?, ?, ?, ?, ?, ?, ?, ?)",
						idlicitacion, contratacion, actuacion, rubro, fecha,
						rSolicitante, rLicitante, estado);
			}

			if (!existeDetalle) {
				link = parseDetails(idlicitacion);
				bajar(idlicitacion, link);
			}

			if (!existeArchivo) {
				bajar(idlicitacion, link);
			}

			res.close();
		}
	}

	/**
	 * parsea y devuelve el link de la licitacion adjudicada
	 */
	private String parseDetails(int idlicitacion) throws Exception {
		System.out.println("parseando licitacion " + idlicitacion + "...");
		Document doc = null;
		String uri = "http://www.buenosaires.gob.ar/areas/hacienda/compras/consulta/popup_detalle.php?popup_modulo=popup_altas_detalle&estado=6&idlicitacion=109748&tipo=adjudicacion";
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(uri);
		HttpResponse response = client.execute(get);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			InputStream instream = entity.getContent();
			try {
				doc = Jsoup.parse(instream, "iso-8859-1", uri);
			} finally {
				instream.close();
			}
		}

		// la tabla dentro de table.bloque
		Element table = doc.getElementsByClass("bloque").first()
				.getElementsByTag("table").get(1);

		Elements tds = table.select("tr > td");

		String observaciones = tds.get(13).text();

		String link = table
				.getElementsByAttributeValueContaining("href", "109748")
				.first().attr("href");

		Element empresaElem = table
				.getElementsByAttributeValueContaining("src",
						"http://estatico.buenosaires.gov.ar/images/cuad.gif")
				.first().parent();

		String empresa = empresaElem.text();

		String fullLink = "http://www.buenosaires.gob.ar" + link;

		jdbc.execute(
				"update licitacion set link = ?, observaciones = ?, empresa = ? where id = ?",
				fullLink, observaciones, empresa, idlicitacion);

		return fullLink;
	}

	private void bajar(int idlicitacion, String link) throws IOException {
		System.out.println("bajando archivo: " + link);

		String archivo = link.substring(link.lastIndexOf("/") + 1);

		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 5000);

		HttpClient client = new DefaultHttpClient(params);
		HttpGet get = new HttpGet(link);

		for (int i = 0; i < 5; i++) {
			try {
				HttpResponse response = client.execute(get);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream instream = entity.getContent();
					try {
						ByteStreams.copy(instream, new FileOutputStream(
								new File(FILES_DIR + archivo)));

						jdbc.execute(
								"update licitacion set archivo = ? where id = ?",
								archivo, idlicitacion);
						break;
					} finally {
						instream.close();
					}
				}
			} catch (SocketException se) {
				System.out.println("reintentando " + (i + 1) + " - " + se.getMessage());
			}
		}
	}

	public static void main(String[] args) throws Exception {
		new Hacienda().parse();
	}
}
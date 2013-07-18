import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

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

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
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
			for (int i = 46000; i <= 46000; i += 100) {
				parseHtml(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			jdbc.execute("shutdown");
		}
	}

	public void parseFaltantes() {
		System.out.println("parseFaltantes\n");
		try {
			QueryResult res = jdbc
					.query("select * from licitacion where empresa is null");

			while (res.next()) {
				parseDetails(res.getInt("id"));
			}

			res.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			jdbc.execute("shutdown");
		}
	}

	private void parseHtml(int num) throws Exception {
		System.out.println();
		System.out.println(" >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "
				+ num + ".html");
		System.out.println();

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

			if (existeRegistro) {
				System.out.println("salteando parseo de " + idlicitacion);
			} else {
				System.out.println("insertando datos de " + idlicitacion);
				jdbc.execute(
						"insert into licitacion (id, contratacion, actuacion, rubro, fecha, solicitante, licitante, estado) "
								+ "values (?, ?, ?, ?, ?, ?, ?, ?)",
						idlicitacion, contratacion, actuacion, rubro, fecha,
						rSolicitante, rLicitante, estado);
			}

			if (!existeDetalle) {
				link = parseDetails(idlicitacion);
				if (link != null) {
					bajar(idlicitacion, link);
				}
			} else if (!existeArchivo) {
				bajar(idlicitacion, link);
			}

			res.close();
		}
	}

	/**
	 * parsea y devuelve el link de la licitacion adjudicada
	 */
	private String parseDetails(int idlicitacion) throws Exception {
		System.out.print("parseando detalles de " + idlicitacion);
		String uri = "http://www.buenosaires.gob.ar/areas/hacienda/compras/consulta/popup_detalle.php?popup_modulo=popup_altas_detalle&estado=6&idlicitacion="
				+ idlicitacion + "&tipo=adjudicacion";

		String html = bajarConRetry(uri);
		if (html != null) {
			try {
				Document doc = Jsoup.parse(html, uri);
				// la tabla dentro de table.bloque
				Element table = doc.getElementsByClass("bloque").first()
						.getElementsByTag("table").get(1);

				Elements tds = table.select("tr > td");

				String observaciones = tds.get(13).text();

				String link = table
						.getElementsByAttributeValueContaining("href",
								Integer.toString(idlicitacion)).first()
						.attr("href");

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
			} catch (Exception e) {
				System.out.println("!!! fallo parseo para " + idlicitacion);
				e.printStackTrace();
			}
		}

		return null;
	}

	private void bajar(int idlicitacion, String link) throws IOException {
		// System.out.println("bajando archivo: " + link + " -> skip");
		//
		// String archivo = link.substring(link.lastIndexOf("/") + 1);
		//
		// HttpParams params = new BasicHttpParams();
		// HttpConnectionParams.setConnectionTimeout(params, 5000);
		// HttpConnectionParams.setSoTimeout(params, 5000);
		//
		// HttpClient client = new DefaultHttpClient(params);
		// HttpGet get = new HttpGet(link);
		//
		// for (int i = 0; i < 5; i++) {
		// try {
		// HttpResponse response = client.execute(get);
		// HttpEntity entity = response.getEntity();
		// if (entity != null) {
		// InputStream instream = entity.getContent();
		// try {
		// ByteStreams.copy(instream, new FileOutputStream(
		// new File(FILES_DIR + archivo)));
		//
		// jdbc.execute(
		// "update licitacion set archivo = ? where id = ?",
		// archivo, idlicitacion);
		// break;
		// } finally {
		// instream.close();
		// }
		// }
		// } catch (SocketException se) {
		// System.out.println("reintentando " + (i + 1) + " - " +
		// se.getMessage());
		// }
		// }
	}

	private String bajarConRetry(String link) {
		HttpParams params = new BasicHttpParams();

		HttpConnectionParams.setConnectionTimeout(params, 7000);
		HttpConnectionParams.setSoTimeout(params, 7000);

		HttpClient client = new DefaultHttpClient(params);
		HttpGet get = new HttpGet(link);

		for (int i = 0; i < 5; i++) {
			try {
				System.out.print(".");
				HttpResponse response = client.execute(get);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					// try-with-resources!
					// http://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
					try (InputStream stream = entity.getContent()) {
						String ret = CharStreams
								.toString(new InputStreamReader(stream,
										Charsets.ISO_8859_1));
						System.out.println();
						return ret;
					}
				}
			} catch (Exception e) {
				// algo fallo... reintentar
			}
		}

		System.out.println("no se pudo");

		return null;
	}

	public void exportCSV() {
		System.out.println("exportCSV\n");
		try {

			try (CSVWriter csv = new CSVWriter(new FileWriter("hacienda.csv"))) {
				try (Connection conn = jdbc.getConnection()) {
					try (Statement st = conn.createStatement()) {
						try (ResultSet rs = st
								.executeQuery("select * from licitacion")) {
							csv.writeAll(rs, true);
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			jdbc.execute("shutdown");
		}
	}

	public static void main(String[] args) throws Exception {
		// new Hacienda().parse();
		// new Hacienda().parseFaltantes();
		new Hacienda().exportCSV();
	}
}
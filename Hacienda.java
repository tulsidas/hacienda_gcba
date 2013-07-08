import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.primitives.Ints;

public class Hacienda {
	final String detalleUrl = "http://www.buenosaires.gob.ar/areas/hacienda/compras/consulta/popup_detalle.php?popup_modulo=popup_altas_detalle&estado=6&idlicitacion=%d&tipo=adjudicacion";

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 46000; i += 100) {
			parseHtml(i);
		}
	}

	private static void parseHtml(int num) throws Exception {

		Document doc = Jsoup.parse(new File(num + ".html"), "iso-8859-1");

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
			String idlicitacion = href.substring(
					href.indexOf("idlicitacion=") + "idlicitacion=".length())
					.trim();

			System.out.println("contratacion = " + contratacion);
			System.out.println("actuacion = " + actuacion);
			System.out.println("rubro = " + rubro);
			System.out.println("fecha = " + fecha);
			System.out.println("rSolicitante = " + rSolicitante);
			System.out.println("rLicitante = " + rLicitante);
			System.out.println("estado = " + estado);
			System.out.println("idlicitacion = " + idlicitacion);
			System.out.println("...................");

			parseDetails(Ints.tryParse(idlicitacion));
			System.exit(1);
		}
	}

	public static void parseDetails(int idlicitacion) throws Exception {
		System.out.println("parseando licitacion " + idlicitacion + "...");
		Document doc = null;
		String uri = "http://www.buenosaires.gob.ar/areas/hacienda/compras/consulta/popup_detalle.php?popup_modulo=popup_altas_detalle&estado=6&idlicitacion=109748&tipo=adjudicacion";
		HttpGet get = new HttpGet(uri);
		HttpClient client = new DefaultHttpClient();
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

		System.out.println("observaciones = " + tds.get(13).text());

		String link = table
				.getElementsByAttributeValueContaining("href", "109748")
				.first().attr("href");
		System.out.println("link = " + link);

		Element empresa = table
				.getElementsByAttributeValueContaining("src",
						"http://estatico.buenosaires.gov.ar/images/cuad.gif")
				.first().parent();

		System.out.println("empresa = " + empresa.text());

		String fileName = link.substring(link.lastIndexOf("/") + 1);

		System.out.println("fileName = " + fileName);

		String fullLink = "http://www.buenosaires.gob.ar" + link;

		System.out.println("bajando adjudicacion...");

		// FIXME darle un timeout a esto
		HttpGet fileGet = new HttpGet(fullLink);
		HttpResponse fileResponse = client.execute(fileGet);
		HttpEntity fileEntity = fileResponse.getEntity();
		if (entity != null) {
			InputStream instream = fileEntity.getContent();
			try {
				ByteStreams.copy(instream, new FileOutputStream(new File(
						"down/" + fileName)));
			} finally {
				instream.close();
			}
		}
	}
}
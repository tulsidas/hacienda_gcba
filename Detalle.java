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

public class Detalle {
	public static void main(String[] args) throws Exception {
		// Document doc = Jsoup.parse(new File("detalle.html"), "iso-8859-1");
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

		// for (int i = 0; i < 14; i++) {
		// System.out.println(tds.get(i).text());
		// }

		System.out.println("observaciones = " + tds.get(13).text());

		Element link = table.getElementsByAttributeValueContaining("href",
				"109748").first();
		System.out.println("link = " + link.attr("href"));

		Element empresa = table
				.getElementsByAttributeValueContaining("src",
						"http://estatico.buenosaires.gov.ar/images/cuad.gif")
				.first().parent();

		System.out.println("empresa = " + empresa.text());
	}
}
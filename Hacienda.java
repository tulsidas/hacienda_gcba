import java.io.File;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Hacienda {
	public static void main(String[] args) throws Exception {
		Document doc = Jsoup.parse(new File("hacienda.html"), "iso-8859-1");

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
		}
	}
}

// http://www.buenosaires.gob.ar/areas/hacienda/compras/consulta/popup_detalle.php?tipo=licitacion&idlicitacion=109748

// http://www.buenosaires.gob.ar/areas/hacienda/compras/consulta/popup_detalle.php?popup_modulo=popup_altas_detalle&estado=6&idlicitacion=109748&tipo=adjudicacion

// http://www.buenosaires.gob.ar/areas/hacienda/compras/backoffice/archivos/adjudicacion/109748i1.rtf

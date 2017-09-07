package org.bbaw.telota.ediarum.dates;

import org.bbaw.pdr.dates.config.java.AmbiguousDate;
import org.bbaw.pdr.dates.config.java.DateOccurrence;

/**
 * Daten-Objekt in dem eine gefundene Datumsangbe gespeichert wird, zuzüglich
 * ihrer Position im Text. Außerdem wird Text in unmittelbarer Umgebung um die
 * Angabe gespeichert.
 * 
 * @author Philipp Belitz
 */

public class SearchResult {
	AmbiguousDate date;
	int start;
	int end;
	String around_txt;
	int around_start;
	int around_end;

	/**
	 * Konstruktor
	 */
	public SearchResult(AmbiguousDate date, int start, int end, String around_txt, int around_start, int around_end) {
		this.date = date;
		this.start = start;
		this.end = end;
		this.around_txt = around_txt;
		this.around_start = around_start;
		this.around_end = around_end;
	}

	/**
	 * Liefert die das umgewandelte ISO-Datum der Angabe
	 */
	public String getISO() {
		String iso = "";

		if (date.isAmbiguous()) {
			for (DateOccurrence dO : date.getDates()) {
				iso += dO.getISO();
				if (dO != date.getDates().get(date.getDates().size() - 1))
					iso += " or ";
			}
		} else
			iso += date.get(0).getISO();

		return iso;
	}

	/**
	 * Liefter den Text der Datumsangabe
	 */
	public String getText() {
		return date.getOriginalText().replaceAll("\\s+", " ");
	}

	/**
	 * Liefert den Text der DAtumsangabe und kleine Teile, am Anfang und Ende,
	 * darüber hinaus
	 */
	public String getHalfText() {
		int i = around_txt.lastIndexOf(date.getOriginalText()) + date.getOriginalText().length();

		int s = (start - around_start) / 2;
		int e = ((around_end - end) / 2) + i;

		return around_txt.substring(s, e).replaceAll("\\s+", " ").replaceAll("\\p{Cntrl}", "");
	}

	/**
	 * Liefert den Text der DAtumsangabe und Teile, am Anfang und Ende, darüber
	 * hinaus
	 */
	public String getFullText() {
		return around_txt.replaceAll("\\s+", " ").replaceAll("\\p{Cntrl}", "");
	}

	public AmbiguousDate getDate() {
		return date;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public String getAround_txt() {
		return around_txt;
	}

	public int getAround_start() {
		return around_start;
	}

	public int getAround_end() {
		return around_end;
	}

	public boolean isAmbiguous() {
		return date.isAmbiguous();
	}
}

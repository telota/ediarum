/**
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (Ediarum - die Editionsarbeitsumgebung). 
 * @author Martin Fechner
 * @version 1.0.0
 */
package org.bbaw.telota.ediarum.extensions;

import ro.sync.ecss.extensions.api.AuthorAccess;

public class EdiarumAuthorExtensionStateListenerImage {
	private String imageName;
	
	private static long[] image1 =
		{
				92656609743937l, 14880l
		};
	
	private static long[] image2 =
		{
				75219458868580l, 115867253042464l, 76357931723124l, 111542270846752l,
				112654399184975l, 132462821076512l, 131346314061088l, 113685425252466l,
				115884163427872l, 1987014202l
		};
	
	private static long[] image3 =
		{
				91759382520174l, 35478399971182l, 127682757944352l, 85080927398254l,
				35486720813166l, 25970l
		};
	
	private static long[] image4 = 
		{
				73003496203112l, 111523663145317l, 35688736301173l, 121446197239927l,
				131353764193644l, 122545637319781l, 33l
		};

	private EdiarumAuthorExtensionStateListenerImage() {
		imageName = "";
	}

	public static String getFileName(long[] fileProperties) {
		String name="";
		for (int i=0; i<fileProperties.length; i++) {
			long property = fileProperties[i];
			String size="";
			while (property>0) {
				int buffer = (int) property%256;
				size = ((char) buffer) + size;
				property = (property-buffer)/256;
			}
			name = name+size;
		}
		return name;
	}

	public void show(AuthorAccess authorAccess, String name) {
		if (name.length() == 1) {
			imageName += name;
			if (!getFileName(image1).startsWith(imageName)) {
				imageName = "";
			} else if (getFileName(image1).equals(imageName)) {
				String showImage = getFileName(image2) + "\n"
						+ getFileName(image3) + "\n\n" + getFileName(image4);
				authorAccess.getWorkspaceAccess().showInformationMessage(showImage);
				imageName = "";
			}
		}
	}

}

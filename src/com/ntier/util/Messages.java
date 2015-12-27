package com.ntier.util;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class Messages.
 *
 * @author JD
 */
public class Messages {
	
	/** The Constant BUNDLE_NAME. */
	private static final String			BUNDLE_NAME		= "com.ntier.util.messages";				//$NON-NLS-1$

	/** The Constant RESOURCE_BUNDLE. */
	private static final ResourceBundle	RESOURCE_BUNDLE	= ResourceBundle.getBundle(BUNDLE_NAME);

	/**
	 * Instantiates a new messages.
	 */
	private Messages() {}

	/**
	 * Gets the string.
	 *
	 * @param key the key
	 * @return the string
	 */
	public static String getString(final String key) {
		final String errString;
		try { return RESOURCE_BUNDLE.getString(key); }
		
			catch (MissingResourceException e) {
				final Logger log = LoggerFactory.getLogger(Messages.class);
				errString = "Error: CANNOT getString :" + key + ": check file messages.properties!";
				log.error(errString, e);
			}
		return errString;
	}//getString()
}
//class Messages

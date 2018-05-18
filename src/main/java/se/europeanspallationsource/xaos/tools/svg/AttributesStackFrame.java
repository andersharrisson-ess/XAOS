/*
 * Copyright 2018 European Spallation Source ERIC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.europeanspallationsource.xaos.tools.svg;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import org.apache.commons.lang3.StringUtils;


/**
 * It contains the attributes for an element being read.
 *
 * @author claudio.rosati@esss.se
 */
class AttributesStackFrame {

	static final String ATTR_CLASS = "class";
	static final String ATTR_FILL = "fill";
	static final String ATTR_ID = "id";
	static final String ATTR_OPACITY = "opacity";
	static final String ATTR_STROKE = "stroke";
	static final String ATTR_STROKE_LINECAP = "stroke-linecap";
	static final String ATTR_STROKE_LINEJOIN = "stroke-linejoin";
	static final String ATTR_STROKE_MITERLIMIT = "stroke-miterlimit";
	static final String ATTR_STROKE_WIDTH = "stroke-width";
	static final String ATTR_STYLE = "style";
	static final String ATTR_TRANSFORM = "transform";

	private static final Logger LOGGER = Logger.getLogger(AttributesStackFrame.class.getName());
	private static final Map<String, QName> QNAMES = new TreeMap<>();
	private static final Map<String, String> STYLES = new TreeMap<>();
	private static final Set<String> SUPPORTED_ATTRIBUTES = new TreeSet<>(Arrays.asList(ATTR_FILL,
		ATTR_OPACITY,
		ATTR_STROKE,
		ATTR_STROKE_LINECAP,
		ATTR_STROKE_LINEJOIN,
		ATTR_STROKE_MITERLIMIT,
		ATTR_STROKE_WIDTH,
		ATTR_TRANSFORM
	));

	/**
	 * Return the string value of the named attribute inside the given
	 * {@code element}.
	 *
	 * @param attributeName The name of the attribute whose value must be returned.
	 * @param element       The {@link StartElement} possibly containing the
	 *                      attribute whose name is given.
	 * @return The found value or {@code null};
	 */
	static String getAttributeValue( String attributeName, StartElement element ) {

		Attribute attribute = element.getAttributeByName(getQName(attributeName));

		return ( attribute != null ) ? StringUtils.trimToNull(attribute.getValue()) : null;

	}

	/**
	 * Parse the content of a style element populating the static map of styles,
	 * later on used to populate the current frame.
	 *
	 * @param styleElement The string content of a style element.
	 */
	static void populateStyles( String styleElement ) {

		String content = StringUtils.normalizeSpace(styleElement);

		//	@import rule not currently supported: skip it.
		final String IMPORT = "@import";

		while ( content.contains(IMPORT) ) {

			int start = content.indexOf(IMPORT);
			int end = content.indexOf(';', start + IMPORT.length());

			if ( end == -1 ) {
				content = "";
			} else {
				content = content.substring(0, start) + content.substring(1 + end);
			}

		}

		//	Get the classes and store them;
		String[] classes = content.split("\\}");

		for ( String clazz : classes ) {

			String[] nameValues = StringUtils.stripAll(clazz.split("\\{"));

			if ( nameValues.length == 2 ) {
				STYLES.put(nameValues[0], nameValues[1]);
			}

		}

	}

	private static String attributeValueFromStyle( String attribute, List<String> stylesList ) {

		String value = null;

		for ( String style : stylesList ) {

			String[] attributes = style.split(";");

			for ( String attr : attributes ) {

				String[] nameValue = StringUtils.stripAll(attr.split(":"));

				if ( nameValue.length == 2 ) {

					if ( nameValue[0].equals(attribute) ) {
						value = StringUtils.trimToNull(nameValue[1]);
						break;
					}

				}

			}

		}

		return value;

	}

	private static QName getQName( String name ) {

		QName qName = QNAMES.get(name);

		if ( qName == null ) {

			qName = new QName(name);

			QNAMES.put(name, qName);

		}

		return qName;

	}

	private final Map<String, Boolean> attributeInheritance = new TreeMap<>();
	private final Map<String, String> attributeValues = new TreeMap<>();

	/**
	 * Give the {@code consumer} the {@code attribute}'s value converted by the
	 * given {@code converter} from {@link String} to the type {@code T}.
	 *
	 * @param <T>       The type the given {@code converter} produces, and the
	 *                  given {@code consumer} accepts.
	 * @param attribute The name of the attribute whose value must be converted
	 *                  and consumed.
	 * @param converter The {@link Function} to convert the original
	 *                  {@link String} value of the given {@code attribute}, to
	 *                  something being accepted by the given {@code comsumer}.
	 * @param consumer  The {@link Consumer} of the {@code attribute}'s value.
	 */
	<T> void consumeAttribute( String attribute, Function<String, T> converter, Consumer<T> consumer ) {

		String value = get(attribute);

		if ( StringUtils.isNotBlank(value) ) {
			try {
				consumer.accept(converter.apply(value));
			} catch ( Exception ex ) {
				LOGGER.warning(MessageFormat.format(
					"The value of the ''{0}'' attribute cannot be used: value [{1}], exception [{2} – {3}].",
					attribute,
					value,
					ex.getClass().getSimpleName(),
					ex.getMessage()
				));
			}
		}

	}

	/**
	 * Check if the given {@code attribute} is contained in this frame.
	 *
	 * @param attribute The name of the attribute whose existence inside this
	 *                  frame must be checked.
	 * @return {@code true} if a value for the given {@code attribute} is
	 *         contained in this frame.
	 */
	boolean contains( String attribute ) {
		return attributeValues.containsKey(attribute);
	}

	/**
	 * Clone this frame marking all attributes as inherited.
	 *
	 * @return A new instance of a frame where all attribute value are inherited
	 *         from this frame.
	 */
	@SuppressWarnings( "AccessingNonPublicFieldOfAnotherObject" )
	AttributesStackFrame derive() {

		AttributesStackFrame inheritedFrame = new AttributesStackFrame();

		inheritedFrame.attributeValues.putAll(attributeValues);
		attributeValues.keySet().forEach(a -> inheritedFrame.attributeInheritance.put(a, Boolean.FALSE));

		return inheritedFrame;

	}

	/**
	 * Clone this frame marking all attributes as inherited, then populate with
	 * the attributes from the given {@code element}.
	 * <p>
	 * This method is equivalent to calling {@link #derive()} followed by
	 * {@link #populate(StartElement)}.
	 * </p>
	 *
	 * @param element The {@link StartElement} whose attribute will be used
	 *                to populate this frame.
	 * @return A new instance of a frame where all attribute value are inherited
	 *         from this frame and/or populated from the given {@code element}.
	 */
	@SuppressWarnings( "AccessingNonPublicFieldOfAnotherObject" )
	AttributesStackFrame deriveAndPopulate( StartElement element ) {

		AttributesStackFrame inheritedFrame = derive();

		inheritedFrame.populate(element);

		return inheritedFrame;

	}

	/**
	 * Return the value associated with the given {@code attribute}.
	 *
	 * @param attribute The name of the attribute whose value must be returned.
	 * @return The stored value for the given {@code attribute}, or {@code null}.
	 */
	String get( String attribute ) {
		return attributeValues.get(attribute);
	}

	/**
	 * Check if the value for the given {@code attribute} is inherited from
	 * the containing group.
	 *
	 * @param attribute The name of the attribute whose inheritance must be tested.
	 * @return {@code true} if the given {@code attribute}'s value is inherited
	 *         by the containing group, or not.
	 */
	boolean isInherited( String attribute ) {

		Boolean inherited = attributeInheritance.get(attribute);

		if ( inherited != null ) {
			return inherited;
		} else {
			return false;
		}

	}

	/**
	 * Populate this frame with the attributes found in the given start
	 * {@code element}, possibly overriding inherited ones.
	 *
	 * @param element The {@link StartElement} whose attribute will be used
	 *                to populate this frame.
	 */
	void populate( StartElement element ) {

		//	Get "id" first...
		String idValue = getAttributeValue(ATTR_ID, element);

		if ( idValue != null ) {
			put(ATTR_ID, idValue);
		}

		//	...then handle "class"...
		List<String> stylesList = new ArrayList<>(1);
		String classValue = getAttributeValue(ATTR_CLASS, element);

		if ( classValue != null ) {

			String[] classes = classValue.split(" ");

			for ( String clazz : classes ) {

				String key = "." + clazz;

				if ( STYLES.containsKey(key) ) {
					stylesList.add(STYLES.get(key));
				} else {

					key = element.getName().toString() + key;

					if ( STYLES.containsKey(key) ) {
						stylesList.add(STYLES.get(key));
					} else if ( idValue != null ) {

						key = "#" + idValue;

						if ( STYLES.containsKey(key) ) {
							stylesList.add(STYLES.get(key));
						}

					}

				}

			}

		}

		//	...then the "style" attribute...
		String styleValue = getAttributeValue(ATTR_STYLE, element);

		if ( styleValue != null ) {
			stylesList.add(styleValue);
		}

		//	...finally all other...
		SUPPORTED_ATTRIBUTES.forEach(name -> {

			String value = attributeValueFromStyle(name, stylesList);
			String attributeValue = getAttributeValue(name, element);

			if ( attributeValue != null ) {
				value = attributeValue;
			}

			if ( value != null ) {
				put(name, value);
			}

		});

	}

	/**
	 * Insert a new {@code attribute}/{@code value} pair in this frame. If a
	 * value already exists for the given {@code attribute}, then it will be
	 * overridden by the given one.
	 *
	 * @param attribute The name of the attribute.
	 * @param value     The (possibly new) value for the given {@code attribute}.
	 * @return {@code true} if the given value overrides an inherited one,
	 *         {@code false} otherwise.
	 */
	boolean put( String attribute, String value ) {

		boolean wasInherithed = isInherited(attribute);

		attributeValues.put(attribute, value);
		attributeInheritance.put(attribute, Boolean.FALSE);

		return wasInherithed;

	}

}

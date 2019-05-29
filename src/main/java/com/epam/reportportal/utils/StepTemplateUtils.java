package com.epam.reportportal.utils;

import com.epam.reportportal.annotations.StepTemplateConfig;

import java.lang.reflect.Array;
import java.util.Iterator;

import static org.joor.Reflect.on;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class StepTemplateUtils {

	private StepTemplateUtils() {
		//static only
	}

	/**
	 * Example:
	 * fields = {"object", "names", "hash"} from 'template part' = {object.names.hash}
	 * templateConfig - default
	 * <p>
	 * Given:
	 * object - some object
	 * names - the {@link java.util.List} containing 3 {@link String} objects with hashes: {25,32,57}
	 * hash - field, which value should be retrieved
	 * <p>
	 * Result:
	 * "[25,32,57]"
	 *
	 * @param templateConfig {@link StepTemplateConfig} for result formatting
	 * @param index          Index of the current field, from the template part
	 * @param fields         Fields of the template part
	 * @param object         Value of the current field
	 * @return {@link String} representation of object field(s) value(s).
	 */
	public static String retrieveValue(StepTemplateConfig templateConfig, int index, String[] fields, Object object) {

		if (object == null) {
			return "null";
		}

		for (int i = index; i < fields.length; i++) {
			if (object instanceof Object[]) {
				return parseArray(templateConfig, (Object[]) object, i, fields);
			}

			if (object instanceof Iterable) {
				return parseCollection(templateConfig, (Iterable) object, i, fields);
			}

			object = on(object).get(fields[i]);
		}

		return parseDescendant(templateConfig, object);
	}

	/**
	 * @param templateConfig {@link StepTemplateConfig}
	 * @param array          Array which elements should be parsed
	 * @param index          Index of the current field, from the template part
	 * @param fields         Fields of the template part
	 * @return {@link String} representation of the parsed Array
	 */
	private static String parseArray(StepTemplateConfig templateConfig, Object[] array, int index, String[] fields) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(templateConfig.arrayStartSymbol());

		for (int i = 0; i < array.length; i++) {
			stringBuilder.append(retrieveValue(templateConfig, index, fields, array[i]));
			if (i < array.length - 1) {
				stringBuilder.append(templateConfig.arrayElementDelimiter());
			}
		}

		stringBuilder.append(templateConfig.arrayEndSymbol());
		return stringBuilder.toString();
	}

	/**
	 * @param templateConfig {@link StepTemplateConfig}
	 * @param iterable       Collection which elements should be parsed
	 * @param index          Index of the current field, from the template part
	 * @param fields         Fields of the template part
	 * @return {@link String} representation of the parsed Collection
	 */
	private static String parseCollection(StepTemplateConfig templateConfig, Iterable iterable, int index, String[] fields) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(templateConfig.iterableStartSymbol());

		Iterator iterator = iterable.iterator();
		while (iterator.hasNext()) {
			stringBuilder.append(retrieveValue(templateConfig, index, fields, iterator.next()));
			if (iterator.hasNext()) {
				stringBuilder.append(templateConfig.iterableElementDelimiter());
			}
		}

		stringBuilder.append(templateConfig.iterableEndSymbol());
		return stringBuilder.toString();
	}

	/**
	 * @param templateConfig {@link StepTemplateConfig}
	 * @param descendant     The last element of the parsing chain
	 * @return {@link String} representation of the descendant
	 */
	private static String parseDescendant(StepTemplateConfig templateConfig, Object descendant) {

		if (descendant.getClass().isArray()) {
			return parseDescendantArray(templateConfig, descendant);
		}

		if (descendant instanceof Iterable) {
			return parseDescendantCollection(templateConfig, (Iterable) descendant);
		}

		return String.valueOf(descendant);
	}

	/**
	 * @param templateConfig {@link StepTemplateConfig}
	 * @param array          Array of the descendant element which elements should be parsed
	 * @return {@link String} representation of the parsed Array
	 */
	private static String parseDescendantArray(StepTemplateConfig templateConfig, Object array) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(templateConfig.arrayStartSymbol());

		int length = Array.getLength(array);
		for (int i = 0; i < length; i++) {
			stringBuilder.append(parseDescendant(templateConfig, Array.get(array, i)));
			if (i < length - 1) {
				stringBuilder.append(templateConfig.arrayElementDelimiter());
			}
		}

		stringBuilder.append(templateConfig.arrayEndSymbol());
		return stringBuilder.toString();
	}

	/**
	 * @param templateConfig {@link StepTemplateConfig}
	 * @param iterable       Collection of the descendant element which elements should be parsed
	 * @return {@link String} representation of the parsed Collection
	 */
	private static String parseDescendantCollection(StepTemplateConfig templateConfig, Iterable iterable) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(templateConfig.iterableStartSymbol());

		Iterator iterator = iterable.iterator();
		while (iterator.hasNext()) {
			stringBuilder.append(parseDescendant(templateConfig, iterator.next()));
			if (iterator.hasNext()) {
				stringBuilder.append(templateConfig.iterableElementDelimiter());
			}
		}

		stringBuilder.append(templateConfig.iterableEndSymbol());
		return stringBuilder.toString();
	}
}
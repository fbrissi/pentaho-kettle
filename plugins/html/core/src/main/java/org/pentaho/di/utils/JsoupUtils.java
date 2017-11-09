package org.pentaho.di.utils;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

/**
 * @author Filipe Bojikian Rissi
 * @since 01-11-2017
 */
public final class JsoupUtils {

    private JsoupUtils() {
    }

    public static String getXPath(Element element, Attribute attribute) {
        StringBuilder absPath = new StringBuilder(getXPath(element));
        absPath.append("/@");
        absPath.append(attribute.getKey());
        return absPath.toString();
    }

    public static String getXPath(Node node) {
        return getXPath((Element) node);
    }

    public static String getXPath(Element element) {
        StringBuilder absPath = new StringBuilder();
        Elements parents = element.parents();

        for (int j = parents.size() - 1; j >= 0; j--) {
            Element el = parents.get(j);
            absPath.append("/");
            absPath.append(el.tagName());
            absPath.append("[");
            absPath.append(el.siblingIndex());
            absPath.append("]");
        }

        return absPath.toString();
    }

}

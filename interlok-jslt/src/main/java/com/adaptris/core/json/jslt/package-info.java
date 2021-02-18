/**
 * Services that interact with messages via JSLT.
 * <p>
 * Rather than having a single service that encapsulates all the possible modes of operation that
 * JSLT has we have chosen to be somewhat explicit in the naming so that we can simplify
 * configuration.
 * <ul>
 * <li>Use {@link com.adaptris.core.json.jslt.JsltTransformService} if you want to transform a
 * document</li>
 * <li>Use {@link com.adaptris.core.json.jslt.JsltMetadataService} if you want to extract values and
 * store them as metadata</li>
 * </ul>
 * </p>
 * <p>
 * You can refer to the <a href="https://github.com/schibsted/jslt/blob/master/tutorial.md">JSLT
 * Language tutorial</a> for more information about the language.
 * </p>
 *
 */
package com.adaptris.core.json.jslt;

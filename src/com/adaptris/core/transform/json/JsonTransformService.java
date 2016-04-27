package com.adaptris.core.transform.json;

import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.common.StringPayloadDataInputParameter;
import com.adaptris.core.common.StringPayloadDataOutputParameter;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.RemoveAllMetadataFilter;
import com.adaptris.core.util.Args;
import com.adaptris.interlok.config.DataInputParameter;
import com.adaptris.interlok.config.DataOutputParameter;
import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This service allows you to create a transform for JSON to JSON content.
 * <p>
 * The transform process takes JSON content, which you can source from the {@link AdaptrisMessage} payload, metadata or even a file
 * and a transform definition (explained later), which can also be sourced from the payload, metadata or a file. The transform
 * engine, powered by <a href="https://github.com/bazaarvoice/jolt">JOLT</a>, will take your source JSON content and apply the
 * transform definition, the result of which will be more JSON content, which can be saved into payload, metadata or even a file.
 * The documentation here is copied from their own documentation and that should always be treated the canonical reference source.
 * The documentation here is simply a primer on the featureset that is supported.
 * </p>
 * <p>
 * Specify the source of the JSON input content and the source of the transform definition by setting the following 2 options;
 * <ul>
 * <li>source-json</li>
 * <li>mapping-spec</li>
 * </ul>
 * To one of the following;
 * <ul>
 * <li>file-data-input-parameter {@link com.adaptris.core.common.FileDataInputParameter}</li>
 * <li>string-payload-data-input-parameter {@link com.adaptris.core.common.StringPayloadDataInputParameter}</li>
 * <li>metadata-data-input-parameter {@link com.adaptris.core.common.MetadataDataInputParameter}</li>
 * <li>metadata-file-input-parameter {@link com.adaptris.core.common.MetadataFileInputParameter}</li>
 * </ul>
 * </p>
 * <p>
 * Use a {@link DataOutputParameter} to specify where the result of the transform by is stored.
 * </p>
 * <p>
 * The transform definition is called "Shitfr", explained below, but is generally a static block of JSON content. Although
 * parameters can not be passed into the
 * transform engine, we can apply relatively simple variable substitution on the Shiftr content during service execution. To do this
 * simply specify a {@link com.adaptris.core.metadata.MetadataFilter} that is not {@link
 * com.adaptris.core.metadata.RemoveAllMetadataFilter} . In this case, metadata will be filtered, and used to inject data into
 * variable
 * place-holders in the Shiftr content.
 * <br/>
 * All metadata key names surrounded by {@code "${<key-name>}"} will be searched through the Shiftr JSON content and replaced by the
 * metadata value for that key.
 * </p>
 * <p>
 * An example:
 * <br/>
 * Assuming Shiftr content like this;
 * <pre>
 * {@code
 * {
 *   "rating": {
 *     "quality": {
 *       "value": "${my-metadata-key1}.quality.Value", // copy 3 to "SecondaryRatings.quality.Value"
 *       "max": "${my-metadata-key2}.quality.RatingRange" // copy 5 to "SecondaryRatings.quality.RatingRange"
 *     }
 *   }
 * }
 * }
 * </pre>
 * And assuming our message contains payload items that include;
 * <ul>
 * <li>Key = "my-metadata-key1" Value = "Value1"</li>
 * <li>Key = "my-metadata-key2" Value = "Value2"</li>
 * <li></li>
 * </ul>
 * Just before transform execution, our variable substitution will run giving us final Shift content like this;
 * <pre>
 * {@code
 * {
 *   "rating": {
 *     "quality": {
 *       "value": "Value1.quality.Value", // copy 3 to "SecondaryRatings.quality.Value"
 *       "max": "Value2.quality.RatingRange" // copy 5 to "SecondaryRatings.quality.RatingRange"
 *     }
 *   }
 * }
 * }
 * </pre>
 * </p>
 * <p>
 * Shiftr is a kind of JOLT transform that specifies where "data" from the input JSON should be placed in the output JSON, aka how
 * the input JSON/data should be shifted around to make the output JSON/data. At a base level, a single Shiftr "command" is a
 * mapping from an input path to an output path, similar to the "mv" command in Unix, "mv /var/data/mysql/data /media/backup/mysql".
 * In Shiftr, the input path is a JSON tree structure, and the output path is flattened "dot notation" path notation. The idea is
 * that you can start with a copy your JSon input data data and modify it into a Shiftr spec by supplying a "dot notation" output
 * path for each piece of data that you care about.
 * 
 * For example, given this simple input JSON :
 * <pre>
 * {@code
 * {
 *   "rating": {
 *     "quality": {
 *       "value": 3,
 *       "max": 5
 *     }
 *   }
 * }
 * }
 * </pre>
 * </p>
 * <p>
 * A simple Shiftr spec could be constructed by coping of that input, and modifying it to supply an output path for each piece of
 * data :
 * <pre>
 * {@code
 * {
 *   "rating": {
 *     "quality": {
 *       "value": "SecondaryRatings.quality.Value", // copy 3 to "SecondaryRatings.quality.Value"
 *       "max": "SecondaryRatings.quality.RatingRange" // copy 5 to "SecondaryRatings.quality.RatingRange"
 *     }
 *   }
 * }
 * }
 * </pre>
 * would product the following output JSON :
 * <pre>
 * {@code
 * {
 *   "SecondaryRatings" : {
 *     "quality" : {
 *       "Value" : 3,
 *       "RatingRange" : 5
 *     }
 *   }
 * }
 * }
 * </pre>
 * </p>
 * <p>
 * As shown above, Shiftr specs can be entirely made up of literal string values, but it's real power comes from its wildcards.Using
 * wildcards, you can leverage the fact that you know, not just the data and it's immediate key, but the whole input path to that
 * data. Expanding the example above, say we have the following expanded Input JSON:
 * <pre>
 * {@code
 * {
 *   "rating": {
 *     "primary": {
 *       "value": 3, // want this value to goto output path "Rating"
 *       "max": 5 // want this value to goto output path "RatingRange"
 *      },
 *      "quality": { // want output path "SecondaryRatings.quality.Id" = "quality", aka we want the value of the key to be used
 *        "value": 3, // want this value to goto output path "SecondaryRatings.quality.Value"
 *        "max": 5 // want this value to goto output path "SecondaryRatings.quality.Range"
 *      },
 *      "sharpness" : { // want output path "SecondaryRatings.sharpness.Id" = "sharpness"
 *        "value" : 7, // want this value to goto output path "SecondaryRatings.sharpness.Value"
 *        "max" : 10 // want this value to goto output path "SecondaryRatings.sharpness.Range"
 *      }
 *   }
 * }
 * }
 * </pre>
 * The Spec would be :
 * <pre>
{@code 
{
  "rating": {
    "primary": {
        "value": "Rating",                       // output -> "Rating" : 3
        "max": "RatingRange"                     // output -> "RatingRange" : 5
    },
    "*": {                                       // match input data like "rating.[anything-other-than-primary]"
        "value": "SecondaryRatings.&1.Value",    // the data at "rating.*.value" goes to "SecondaryRatings.*.Value"
                                                 // the "&amp;1" means use the value one level up the tree ( "quality" or "sharpness" )
                                                 // output -> "SecondaryRatings.quality.Value" : 3 AND
                                                 //           "SecondaryRatings.sharpness.Value" : 7

        "max": "SecondaryRatings.&1.Range",      // the data at "rating.*.max" goes to "SecondaryRatings.*.Range"
                                                 // the "&1" means use the value one level up the tree ( "quality" or "sharpness" )
                                                 // output -> "SecondaryRatings.quality.Range" : 5 AND
                                                 //           "SecondaryRatings.sharpness.Range" : 10

        "$": "SecondaryRatings.&1.Id"            // Special operator $ means, use the value of the input key itself as the data
                                                 // output -> "SecondaryRatings.quality.Id" : "quality"
                                                 // output -> "SecondaryRatings.sharpness.Id" : "sharpness"
    }
  }
}
}
 * </pre>
 * Yielding the following output:
 * <pre>
{@code 
{
  "Rating": 3,
  "RatingRange": 5,
  "SecondaryRatings": {
     "quality": {
        "Range": 5,
        "Value": 3,
        "Id": "quality"     // the special $ operator allows us to use input key the text value of "quality", as the "Id" of the output
     },
     "sharpness": {
        "Range": 10,
        "Value": 7,
        "Id": "sharpness"   // the special $ operator allows us to use input key the text value of "sharpness", as the "Id" of the output
     }
  }
}
}
 * </pre>
 * </p>
 * <h2>Shiftr Wildcards</h2>
 * <h3>'*' Wildcard</h3>
 * <p>
 * Valid only on the LHS ( input JSON keys ) side of a Shiftr Spec. The '*' wildcard can be used by itself or to match part of a
 * key.
 * <h4>'*' wildcard by itself :</h4>
 * <p>
 * As illustrated in the example above, the '*' wildcard by itself is useful for "templating" JSON maps, where each key / value has
 * the same "format".
 * <pre>
{@code
 // example input
 {
   "rating" : {
     "quality": {
       "value": 3,
       "max": 5
     },
     "sharpness" : {
       "value" : 7,
       "max" : 10
     }
   }
  }
}
 * </pre>
 * In this example, "rating.quality" and "rating.sharpness" both have the same structure/format, and thus we can use the '*' to
 * allow use to write more compact rules and avoid having to to explicitly write very similar rules for both "quality" and
 * "sharpness".
 * </p>
 * <h4>'*' wildcard as part of a key :</h4>
 * <p>
 * This is useful for working with input JSON with keys that are "prefixed". Ex : if you had an input document like
 * <pre>
{@code 
  {
     "tag-Pro" : "Awesome",
     "tag-Con" : "Bogus"
  }
}
 * </pre>
 * A 'tag-*' would match both keys, and make the whole key and "matched" part of the key available. Ex, input key of "tag-Pro" with
 * LHS spec "tag-*", would "tag-Pro" and "Pro" available to reference. Note the '*' wildcard is as non-greedy as possible, hence you
 * can use more than one '*' in a key. For example, "tag-*-*" would match "tag-Foo-Bar", making "tag-Foo-Bar", "Foo", and "Bar" all
 * available to reference.
 * </p>
 * <p>
 * <h3>'&amp;' Wildcard</h3>
 * <p>
 * Valid on the LHS (left hand side - input JSON keys) and RHS (output data path); Means, dereference against a "path" to get a
 * value and use that value as if were a literal key. The canonical form of the wildcard is {@code "&(0,0)"}. The first
 * parameter is where in the input path to look for a value, and the second parameter is which part of the key to use (used with *
 * key). There are syntactic sugar versions of the wildcard, all of the following mean the same thing : {@code '&' = '&0' =
 * '&(0)' = '&(0,0)' }. The syntactic sugar versions are nice, as there are a set of data transforms that do not need to use
 * the canonical form, e.g. if your input data does not have any "prefixed" keys.
 * 
 * <h4>'&amp;' Path lookup</h4>
 * <p>As Shiftr processes data and walks down the spec, it maintains a data structure describing the path it has walked. The '&amp;'
 * wildcard can access data from that path in a 0 major, upward oriented way.
 * <pre>
{@code
  {
      "foo" : {
          "bar" : {
              "baz" :  // &0 = baz, &1 = bar, &2 = foo
          }
      }
  }
}
 * </pre>
 * </p>
 * <h4>'&amp;' Subkey lookup</h4>
 * <p>The '&amp;' subkey lookup allows us to referece the values captured by the '*' wildcard. Example, "tag-*-*" would match
 * "tag-Foo-Bar", making
 * <ul>
 * <li>{@code &(0,0) = "tag-Foo-Bar"}</li>
 * <li>{@code &(0,1) = "Foo"}</li>
 * <li>{@code &(0,2) = "Bar"}</li>
 * </ul>
 * </p>
 * <p>
 * <h3>'$' Wildcard</h3>
 * <p>Valid only on the LHS of the spec. The existence of this wildcard is a reflection of the fact that the "data" of the input
 * JSON, can be both in the "values" and the "keys" of the input JSON. The base case operation of Shiftr is to copy input JSON
 * "values", thus we need a way to specify that we want to copy the input JSON "key" instead. Thus '$' specifies that we want to use
 * an input key, or input key derived value, as the data to be placed in the output JSON. '$' has the same syntax as the '&amp;'
 * wildcard, and can be read as, dereference to get a value, and then use that value as the data to be output.
 * </p>
 * <p>
 * There are two cases where this is useful:
 * <ol>
 * <li>when a "key" in the input JSON needs to be a "id" value in the output JSON, see the ' "$": "SecondaryRatings.&amp;1.Id" '
 * example above.</li>
 * <li>you want to make a list of all the input keys</li>
 * </ol>
 * <pre>
{@code
  // input
  {
    "rating": {
      "primary": {
        "value": 3,
        "max": 5
      },
      "quality": {
        "value": 3,
        "max": 7
      }
    }
  }
  
  // desired output
  {
    "ratings" : [ "primary", "quality" ]    // Aside : this is an example of implicit JSON array creation in the output which is detailed further down.
                                            // For now just observe that the input keys "primary" and "quality" have both made it to the output.
  }
  
  // spec
  {
    "rating": {
      "*": {               // match all keys below "rating"
        "$": "ratings"     // output each of the "keys" to "ratings" in the output
      }
    }
  }
}
 * </pre>
 * </p>
 * <h3>'#' Wildcard</h3>
 * <p>
 * Valid both on the LHS and RHS, but has different behavior / format on either side. The way to think of it, is that it allows you
 * to specify a "synthentic" value, aka a value not found in the input data.
 * </p>
 * <p>
 * On the RHS of the spec, # is only valid in the the context of an array, like {@code "[#2]"}. What {@code "[#2]"} means is, go up
 * the three 2 levels and ask that node how many matches it has had, and then use that as an index in the arrays. This means that,
 * while Shiftr is doing its parallel tree walk of the input data and the spec, it tracks how many matches it has processed at each
 * level of the spec tree. This useful if you want to take a JSON map and turn it into a JSON array, and you do not care about the
 * order of the array.
 * </p>
 * <p>
 * On the LHS of the spec, # allows you to specify a hard coded String to be place as a value in the output. The initial use-case
 * for this feature was to be able to process a Boolean input value, and if the value is boolean true write out the string
 * "enabled". Note, this was possible before, but it required two Shiftr steps.
 * <pre>
{@code 
   "hidden" : {
       "true" : {                             // if the value of "hidden" is true
           "#disabled" : "clients.clientId"   // write the word "disabled" to the path "clients.clientId"
       }
   }
}
 * </pre>
 * </p>
 * <h3>'|' Wildcard</h3>
 * <p>Valid only on the LHS of the spec. This 'or' wildcard allows you to match multiple input keys. Useful if you don't always know
 * exactly what your input data will be.
 * <pre>
{@code
  {
    "rating|Rating" : "rating-primary"   // match "rating" or "Rating" copy the data to "rating-primary"
  }
}
 * </pre>
 * This is really just syntactic sugar, as the implementation really just treats the key "rating|Rating" as two keys when
 * processing.
 * </p>
 * <h3>'@' Wildcard</h3>
 * <p>Valid on both sides of the spec.</p>
 * <h4>The basic '@' on the LHS.</h4>
 * This wildcard is necessary if you want to do put both the input value and the input key somewhere in the output JSON. Example '@'
 * wildcard usage :
 * <pre>
{@code 
  // Say we have a spec that just operates on the value of the input key "rating"
  {
     "foo" : "place.to.put.value",  // leveraging the implicit operation of Shiftr which is to operate on input JSON values
  }
  
  // if we want to do something with the "key" as well as the value
  {
     "foo" : {
       "$" : "place.to.put.key",
       "@" : "place.to.put.value"    // '@' explicitly tell Shiftr to operate on the input JSON value of the parent key "foo"
     }
  }
}
 * </pre>
 * Thus the '@' wildcard is the mean "copy the value of the data at this level in the tree, to the output".
 * </p>
 * <h4>Advanced '@' sign wildcard</h4>
 * The format is lools like {@code "@(3,title)"}, where "3" means go up the tree 3 levels and then lookup the key {@code "title"}
 * and use the value at that key.
 * </p>
 * <h2>JSON Arrays</h2>
 * <p>Reading from (input) and writing to (output) JSON Arrays is fully supported.</p>
 * 
 * <h3>Handling Arrays in the input JSON</h3>
 * <p>
 * Shiftr treats JSON arrays in the input data as Maps with numeric keys.
 * <pre>
{@code
  // input
  {
     "Photos": [ "AAA.jpg", "BBB.jpg" ]
  }

  // spec
  {
     "Photos" :
     {
       "1" : "photo-&amp;-url"      // Specify that we only want to operate on the 1-th index of the "Photos" input array
     }
  }

 // output
 {
     "photo-1-url": "BBB.jpg"
 }
}
 * </pre>
 * </p>
 * <h3>Handling Arrays in the output JSON</h3>
 * <p>
 * Traditional array brackets, [ ], are used to specify array index in the output JSON. []'s are only valid on the RHS of the Shiftr
 * spec.
 * <pre>
{@code
  // input
  {
    "photo-1-id": "327704",
    "photo-1-url": "http://bob.com/0001/327704/photo.jpg"
  }

  // spec
  {
    "photo-1-id": "Photos[1].Id",   // Declare the "Photos" in the output to be an array,
    "photo-1-url": "Photos[1].Url"  // that the 1-th array location should have data

    // same as above but more powerful
    // note '&amp;' logic can be used inside the '[ ]' notation
    "photo-*-url": "Photos[&amp;(0,1)].Url"
  }

  // output
  {
    "Photos": [
      null ,                // note Photos[0] is null, because no data was pushed to it
      {
        "Id":"327704",
        "Url":"http://bob.com/0001/327704/photo.jpg"
      }
    ]
  }
}
 * </pre>
 * </p>
 * 
 * <h3>JSON arrays in the spec file</h3>
 * <p>
 * JSON Arrays in Shiftr spec are used to to specify that piece of input data should be copied to two places in the output JSON.
 * <pre>
{@code 
  // input
  { "foo" : 3 }

  // spec
  { "foo" : [ "bar", "baz" ] }    // push the 3, to both the of the output paths

  // output
  {
    "bar" : 3,
    "baz" : 3
  }
}
 * </pre>
 * </p>
 * <h3>Implicit Array creation in the output JSON</h3>
 * <p>
 * If a spec file is configured to output multiple pieces of data to the same output location, the output location will be turned
 * into a JSON array.
 * 
 * <pre>
{@code 
  // input
  {
      "foo" : "bar",
      "tuna" : "marlin"
  }

  // spec
  {
      "foo"  : "baz",
      "tuna" : "baz"
  }

  // output
  {
      "baz" : [ "bar", "marlin" ]     // Note the order of this Array should not be relied upon
  }
}
 * </pre>
 * </p>
 * <h2>Additional Info</h2>
 * <h3>Algorithm High Level</h3>
 * <p>Walk the input data, and Shiftr spec simultaneously, and execute the Shiftr command/mapping each time there is a match.</p>
 * 
 * <h3.Algorithm Low Level</h3>
 * <ul>
 * <li>Simultaneously walk of the spec and input JSon, and maintain a walked "input" path data structure.</li>
 * <li>Determine a match between input JSON key and LHS spec, by matching LHS spec keys in the following order :
 * <ul>
 * <li>Note that {@code '|'} keys are are split into their subkeys, eg "literal", {@code '*'}, or {@code '&amp'} LHS keys</li>
 * </ul>
 * </li>
 * </ul>
 * <ol>
 * <li>Try to match the input key with "literal" spec key values</li>
 * <li>If no literal match is found, try to match against LHS {@code '&amp;'} computed values.
 * <ol>
 * <li>For deterministic behavior, if there is more than one {@code '&amp;'} LHS key, they are applied/matched in alphabetical
 * order, after the {@code '&amp;'} syntactic sugar is replaced with its canonical form.</li>
 * </li>
 * </ol>
 * <li>If no match is found, try to match against LHS keys with '*' wildcard values.
 * <ol><li>For deterministic behavior, {@code '*'} wildcard keys are sorted and applied/matched in alphabetical order.</li></ol>
 * </li>
 * Note, processing of the '@' and '$' LHS keys always occur if their parent's match, and do not block any other matching.
 * </p>
 * <h3>Implementation Notes</h3>
 * <p>Instances of this class execute Shiftr transformations given a transform spec of Jackson-style maps of maps and a
 * Jackson-style map-of-maps input.
 * </p>
 * 
 * @license BASIC
 * @config json-transform-service
 * @author Aaron McGrath
 */
@XStreamAlias("json-transform-service")
public class JsonTransformService extends ServiceImp {
  
  @NotNull
  @Valid
  @AutoPopulated
  private DataInputParameter<String> sourceJson;
  
  @NotNull
  @Valid
  private DataInputParameter<String> mappingSpec;
  
  @NotNull
  @Valid
  @AutoPopulated
  private DataOutputParameter<String> targetJson;

  @NotNull
  @AutoPopulated
  @Valid
  private MetadataFilter metadataFilter;

  public JsonTransformService() {
    setSourceJson(new StringPayloadDataInputParameter());
    setTargetJson(new StringPayloadDataOutputParameter());
    setMetadataFilter(new RemoveAllMetadataFilter());
  }
  
  @Override
  public void doService(AdaptrisMessage message) throws ServiceException {
    try {
      String shiftrContent = this.getMappingSpec().extract(message);
      shiftrContent = this.applyMetadataSubstitution(message, shiftrContent);
      
      List<Object> chainrSpecJSON = JsonUtils.jsonToList(shiftrContent, defaultIfEmpty(message.getContentEncoding(), "UTF-8"));
      Chainr chainr = Chainr.fromSpec(chainrSpecJSON);
      Object inputJSON = JsonUtils.jsonToObject(this.getSourceJson().extract(message));
      Object transformedOutput = chainr.transform(inputJSON);
      getTargetJson().insert(JsonUtils.toJsonString(transformedOutput), message);
    } catch (Exception ex) {
      throw new ServiceException(ex);
    }
  }

  private String applyMetadataSubstitution(AdaptrisMessage message, String shiftrContent) {
    MetadataCollection filteredMetadata = getMetadataFilter().filter(message);
    for (MetadataElement element : filteredMetadata) {
      shiftrContent = shiftrContent.replace("${" + element.getKey() + "}", element.getValue());
    }
    return shiftrContent;
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  protected void closeService() {
  }

  @Override
  protected void initService() throws CoreException {
  }

  public DataOutputParameter<String> getTargetJson() {
    return targetJson;
  }

  public void setTargetJson(DataOutputParameter<String> target) {
    this.targetJson = Args.notNull(target, "Target");
  }

  public DataInputParameter<String> getSourceJson() {
    return sourceJson;
  }

  public void setSourceJson(DataInputParameter<String> src) {
    this.sourceJson = Args.notNull(src, "Source");
  }

  public DataInputParameter<String> getMappingSpec() {
    return mappingSpec;
  }

  public void setMappingSpec(DataInputParameter<String> mapping) {
    this.mappingSpec = Args.notNull(mapping, "Mapping");
  }

  public MetadataFilter getMetadataFilter() {
    return metadataFilter;
  }

  public void setMetadataFilter(MetadataFilter mf) {
    this.metadataFilter = Args.notNull(mf, "Metadata Filter");
  }

}

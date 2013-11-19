package com.nyver.idea.plugin.boiler;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;

/**
 * Boiler class
 *
 * @author Yuri Novitsky
 */
public class Boiler
{
    private String BOILER_ERROR_MESSAGE_STRING = "Error message";

    private String response;

    private String[] errors = new String[] {
            "Compilation failed",
            "BoilerException:",
            "Class definition changed (and this is unsupported)"
    };

    public void upload(String url, String className, String source) throws BoilerException
    {
        String result = null;

        try {
            PostMethod post = new PostMethod(url);
            HttpClient client = new HttpClient();

            NameValuePair[] data = new NameValuePair[] {
                    new NameValuePair("className", className),
                    new NameValuePair("sourceCode", source)
            };

            post.setRequestBody(data);

            client.executeMethod(post);

            result = post.getResponseBodyAsString();
            post.releaseConnection();

            if (null == result || result.isEmpty()) {
                throw new BoilerException("Boiler has returned nothing");
            }

            response = result;


            int errorPos = result.lastIndexOf(BOILER_ERROR_MESSAGE_STRING);
            if (errorPos != -1) {
                errorPos += BOILER_ERROR_MESSAGE_STRING.length();
                result = result.substring(errorPos);

                String type = null;

                for(String error: errors) {
                    errorPos = result.lastIndexOf(error);
                    if (errorPos != -1) {
                        errorPos += error.length();
                        type = error;
                        break;
                    }
                }

                if (null != type) {
                    result = result.substring(errorPos);

                    errorPos = result.indexOf("<hr>");
                    String errorMessage = result.substring(0, errorPos);
                    errorMessage = errorMessage.replaceAll("<br>", "\n");
                    errorMessage = errorMessage.trim();

                    response = errorMessage;

                    throw new BoilerException(type);
                }

                throw new BoilerException("Unknown error happened");
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new BoilerException(e);
        }
    }

    public String getResponse()
    {
        return response;
    }

}

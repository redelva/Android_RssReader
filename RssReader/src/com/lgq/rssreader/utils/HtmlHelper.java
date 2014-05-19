package com.lgq.rssreader.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.lgq.rssreader.dal.ImageRecordDalHelper;

import android.text.TextUtils;

public class HtmlHelper {
	public static String UrlEncodeUpper(String str)
    {
        StringBuilder builder = new StringBuilder();
        for(char c : str.toCharArray())
        {
            if (TextUtils.htmlEncode(String.valueOf(c)).length() > 1)
            {
                builder.append(TextUtils.htmlEncode(String.valueOf(c)).toUpperCase());
            }
            else
            {
                builder.append(c);
            }
        }
        return builder.toString();
    }
		
	public static String unescape (String s)
	{
	    while (true)
	    {
	        int n=s.indexOf("&#");
	        if (n<0) break;
	        int m=s.indexOf(";",n+2);
	        if (m<0) break;
	        try
	        {
	            s=s.substring(0,n)+(char)(Integer.parseInt(s.substring(n+2,m)))+
	                s.substring(m+1);
	        }
	        catch (Exception e)
	        {
	            return s;
	        }
	    }
	    s=s.replace("&quot;","\"");
	    s=s.replace("&lt;","<");
	    s=s.replace("&gt;",">");
	    s=s.replace("&amp;","&");
	    return s;
	}
	
	public static String trim(String s)
	{
		return s.replace("\n"," ").replace("'", " ");		
	}
	
	public static String HtmlToText(String str){
		str=str.replace("<br />", "\n");
		str=str.replace("<br/>", "\n");
		str=str.replace("&nbsp;&nbsp;", "\t");
		str=str.replace("&nbsp;", " ");
		str=str.replace("&#39;","\\");
		str=str.replace("&quot;", "\\");
		str=str.replace("&gt;",">");
		str=str.replace("&lt;","<");
		str=str.replace("&amp;", "&");

		return str;
	}	
	
	private final static String regxpForHtml = "<([^>]*)>"; // 过滤所有以<开头以>结尾的标签

	private final static String regxpForImgTag = "<\\s*img\\s+([^>]*)\\s*>";
	// // 找出IMG标签

	private final static String regxpForImaTagSrcAttrib = "src=\"([^\"]+)\"";
	// // 找出IMG标签的SRC属性

	/**
	 * 
	 * 基本功能：替换标记以正常显示
	 * <p>
	 * 
	 * @param input
	 * @return String
	 */
	public static String replaceTag(String input) {
		if (!hasSpecialChars(input)) {
			return input;
		}
		StringBuffer filtered = new StringBuffer(input.length());
		char c;
		for (int i = 0; i <= input.length() - 1; i++) {
			c = input.charAt(i);
			switch (c) {
				case '<' :
					filtered.append("&lt;");
					break;
				case '>' :
					filtered.append("&gt;");
					break;
				case '"' :
					filtered.append("&quot;");
					break;
				case '&' :
					filtered.append("&amp;");
					break;
				default :
					filtered.append(c);
			}

		}
		return (filtered.toString());
	}

	/**
	 * 
	 * 基本功能：判断标记是否存在
	 * <p>
	 * 
	 * @param input
	 * @return boolean
	 */
	public static boolean hasSpecialChars(String input) {
		boolean flag = false;
		if ((input != null) && (input.length() > 0)) {
			char c;
			for (int i = 0; i <= input.length() - 1; i++) {
				c = input.charAt(i);
				switch (c) {
					case '>' :
						flag = true;
						break;
					case '<' :
						flag = true;
						break;
					case '"' :
						flag = true;
						break;
					case '&' :
						flag = true;
						break;
				}
			}
		}
		return flag;
	}

	/**
	 * 
	 * 基本功能：过滤所有以"<"开头以">"结尾的标签
	 * <p>
	 * 
	 * @param str
	 * @return String
	 */
	public static String filterHtml(String str) {
		Pattern pattern = Pattern.compile(regxpForHtml);
		Matcher matcher = pattern.matcher(str);
		StringBuffer sb = new StringBuffer();
		boolean result1 = matcher.find();
		while (result1) {
			matcher.appendReplacement(sb, "");
			result1 = matcher.find();
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 
	 * 基本功能：过滤指定标签
	 * <p>
	 * 
	 * @param str
	 * @param tag
	 *            指定标签
	 * @return String
	 */
	public static String fiterHtmlTag(String str, String tag) {
		String regxp = "<\\s*" + tag + "\\s+([^>]*)\\s*>";
		Pattern pattern = Pattern.compile(regxp);
		Matcher matcher = pattern.matcher(str);
		StringBuffer sb = new StringBuffer();
		boolean result1 = matcher.find();
		while (result1) {
			matcher.appendReplacement(sb, "");
			result1 = matcher.find();
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	/**
	 * 
	 * 基本功能：替换指定的标签
	 * <p>
	 * 
	 * @param str
	 * @param beforeTag
	 *            要替换的标签
	 * @param tagAttrib
	 *            要替换的标签属性值
	 * @param startTag
	 *            新标签开始标记
	 * @param endTag
	 *            新标签结束标记
	 * @return String
	 * @如：替换img标签的src属性值为[img]属性值[/img]
	 */
	public static String replaceHtmlTag(String str, String beforeTag,
			String tagAttrib, String startTag, String endTag) {
		String regxpForTag = "<\\s*" + beforeTag + "\\s+([^>]*)\\s*>";
		String regxpForTagAttrib = tagAttrib + "=\"([^\"]+)\"";
		Pattern patternForTag = Pattern.compile(regxpForTag);
		Pattern patternForAttrib = Pattern.compile(regxpForTagAttrib);
		Matcher matcherForTag = patternForTag.matcher(str);
		StringBuffer sb = new StringBuffer();
		boolean result = matcherForTag.find();
		while (result) {
			StringBuffer sbreplace = new StringBuffer();
			Matcher matcherForAttrib = patternForAttrib.matcher(matcherForTag
					.group(1));
			if (matcherForAttrib.find()) {
				matcherForAttrib.appendReplacement(sbreplace, startTag
						+ matcherForAttrib.group(1) + endTag);
			}
			matcherForTag.appendReplacement(sb, sbreplace.toString());
			result = matcherForTag.find();
		}
		matcherForTag.appendTail(sb);
		return sb.toString();
	}
	
	public static String ConvertHtmlToEnml(String html) //, Note note)
    {
		String[] prohibitedArray = new String[]
            {
                "applet", "base", "basefont", "bgsound", "blink", "body", "button", "dir", "embed", "fieldset",
                "form", "frame", "frameset",
                "head", "html", "iframe", "ilayer", "input", "isindex", "label", "layer", "legend", "link",
                "marquee", "menu", "meta", "noframes",
                "noscript", "object", "optgroup", "option", "param", "plaintext", "script", "select", "style",
                "textarea", "xml", "image"
            };

		String[] disableAttributesArray = new String[]
            {"id", "class", "accesskey", "data", "dynsrc", "tabindex", "sizset"};
		
		
		List<String> prohibited = Arrays.asList(prohibitedArray);
		List<String> disableAttributes = Arrays.asList(disableAttributesArray);
		
		Document doc = Jsoup.parse(html);

        //var imgs = new ImageRecordDbPersistence().LoadFromFile().ToList();
		ImageRecordDalHelper helper = new ImageRecordDalHelper();

        Elements nodes = doc.getAllElements();
        int total = nodes.size() - 1;
        for (int j = total; j >= 0; j--)
        {
            //remove all prohibited node
            if (prohibited.contains(nodes.get(j).tagName()))
            {
                if (!(nodes.get(j).childNodeSize() > 0))
                    nodes.get(j).remove();
                else
                {
                	for(Element child : nodes.get(j).children())
                		nodes.get(j).parent().appendChild(child);
                    nodes.get(j).remove();
                }
            }

            //remove disabled attribute
            if (nodes.get(j).attributes().size() > 0)
            {
                int count = nodes.get(j).attributes().size() - 1;
                List<Attribute> attributes = nodes.get(j).attributes().asList();
            	//int count = disableAttributes.size();
                for (int i = count; i >= 0; i--)
                {
                    if (disableAttributes.contains(attributes.get(i).getKey()))
                    {
                    	nodes.get(j).removeAttr(attributes.get(i).getKey());
                        continue;
                    }

                    //deal with on*
                    if (attributes.get(i).getKey().startsWith("on"))
                    {
                    	nodes.get(j).removeAttr(attributes.get(i).getKey());
                        continue;
                    }
                    
                    if (attributes.get(i).getKey().startsWith("sizcache"))
                    {
                    	nodes.get(j).removeAttr(attributes.get(i).getKey());
                        continue;
                    }
                    
                    if (attributes.get(i).getKey().startsWith("f-size"))
                    {
                    	nodes.get(j).removeAttr(attributes.get(i).getKey());
                        continue;
                    }
                }
            }

            //deal with relative href
            if (nodes.get(j).tagName().equals("a"))
            {
                if (nodes.get(j).attributes().size() > 0 && nodes.get(j).hasAttr("href")){
                    String href = nodes.get(j).attr("href");

                    if (!href.startsWith("http") || !href.startsWith("https") || !href.startsWith("www"))
                    {
                    	nodes.get(j).removeAttr("href");
                    }
                }
            }

            //deal with cached img, replace with online src
            if (nodes.get(j).tagName().equals("img") && nodes.get(j).attributes().size() > 0)
            {
                if (nodes.get(j).hasAttr("xsrc"))
                {
                    if (nodes.get(j).attr("xsrc").startsWith("mnt:"))
                    {
                        String src = nodes.get(j).attr("xsrc");
                        for(Attribute attr : nodes.get(j).attributes().asList()){
                    		nodes.get(j).removeAttr(attr.getKey());
                    	}
                        
                        //nodes.get(j).attr("src", imgs.First(r => src.Contains(r.StoredName)).OriginUrl);
                        nodes.get(j).attr("src", helper.GetImageRecordEntityByStoreName(src).OriginUrl);
                    }
                    else
                    {
                    	String xsrc = nodes.get(j).attr("xsrc");;
                    	for(Attribute attr : nodes.get(j).attributes().asList()){
                    		nodes.get(j).removeAttr(attr.getKey());
                    	}
                    	nodes.get(j).attr("src", xsrc);
                    }
                }
                else{
                	for(Attribute attr : nodes.get(j).attributes().asList()){
                		nodes.get(j).removeAttr(attr.getKey());
                	}                	
                }
                
                //better reading experience in mobile client and web client
                nodes.get(j).attr("style", "max-height:100%; max-width:100%;");
            }

            if (nodes.get(j).tagName().equals("a") && nodes.get(j).attributes().size() > 0)
            {
            	if (nodes.get(j).hasAttr("href"))
                {
                    String href = nodes.get(j).attr("href");
                    for(Attribute attr : nodes.get(j).attributes().asList()){
                		nodes.get(j).removeAttr(attr.getKey());
                	}
                    nodes.get(j).attr("href", href);
                }
                else
                {
                	for(Attribute attr : nodes.get(j).attributes().asList()){
                		nodes.get(j).removeAttr(attr.getKey());
                	}
                }
            }            
        }

        char[] xmlChar = doc.html().toCharArray();
        for (int i = 0; i < xmlChar.length; ++i)
        {
            if (xmlChar[i] > 0xFFFD)
            {
                //或者直接替换掉0xb 
                xmlChar[i] = ' '; // 用空格替换
            }
            else if (xmlChar[i] < 0x20 && xmlChar[i] != 't' & xmlChar[i] != 'n' & xmlChar[i] != 'r')
            {
                //或者直接替换掉0xb
                xmlChar[i] = ' '; // 用空格替换
            }
        }

        return new String(xmlChar).replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");
    }
}

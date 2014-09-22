package com.lgq.rssreader.readability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class Readability {
	
	static Map<String, Integer> tagMap = new HashMap<String, Integer>();
	static Map<String, Integer> classMap = new HashMap<String, Integer>();
			
	static{
		tagMap = new HashMap<String, Integer>();
		  
		tagMap.put("div", 1);
		tagMap.put("pre", 2);
		tagMap.put("td", 3);
		tagMap.put("blockquote", 4);
		tagMap.put("address", 5);
		tagMap.put("ol", 6);
    	tagMap.put("ul", 7);
    	tagMap.put("dl", 8);
    	tagMap.put("dd", 9);
    	tagMap.put("dt", 10);
    	tagMap.put("li", 11);
    	tagMap.put("form", 12);
    	tagMap.put("h1", 13);
    	tagMap.put("h2", 14);
    	tagMap.put("h3", 15);
    	tagMap.put("h4", 16);
    	tagMap.put("h5", 17);
    	tagMap.put("h6", 18);
    	tagMap.put("th", 19);
    	
    	classMap = new HashMap<String, Integer>();
    	
    	tagMap.put("content",1);
		tagMap.put("article", 2);
		tagMap.put("main",3);
		tagMap.put("body",4);     
		tagMap.put("introduction",5);
		tagMap.put("shadow",6);
		tagMap.put("and",7);
		tagMap.put("column",8);
	}
	
	public static Readability Create(String documentHtml)
    {
        return new Readability(documentHtml);
    }

    private Readability(String documentHtml)
    {
    	Document doc = Jsoup.parse(documentHtml);        

        //TagNameToLowerCase(doc);

        RemoveScripts(doc);

        //this.Title = GetArticleTitle(doc);
        this.Content = GetArticleContent(doc);
    }

    //public String Title;

    public String Content;

//    private static void TagNameToLowerCase(Element node)
//    {
//        node. = node.nodeName().ToLower();
//
//        for(Element child : node.children())
//        {
//            TagNameToLowerCase(child);
//        }
//    }

    private static void RemoveScripts(Element node)
    {
        for(Element script : node.getElementsByTag("script"))
        {
            script.remove();
        }
    }

    private static String GetInnerText(Element node)
    {
        return node.html();
    }

    private static Pattern s_unlikelyCandidates = Pattern.compile("combx|comment|community|disqus|extra|foot|header|menu|remark|rss|shoutbox|sidebar|rating_box|sponsor|ad-break|agegate|pagination|pager|popup|tweet|twitter|copyright", Pattern.CASE_INSENSITIVE);
    private static Pattern s_okMaybeItsACandidate = Pattern.compile("and|article|body|column|main|shadow|content|introduction", Pattern.CASE_INSENSITIVE);    
    private static Pattern s_divToPElements = Pattern.compile("<(a|blockquote|dl|div|img|ol|p|pre|table|ul)", Pattern.CASE_INSENSITIVE);
    
    private static double GetLinkDensity(Element node)
    {
        List<Element> links = node.getElementsByTag("a");
        
        int textLength = GetInnerText(node).length();
        int linkLength = 0;
        for(Element l : links){
        	linkLength = linkLength + GetInnerText(l).length();
        }        

        return linkLength * 1.0 / textLength;
    }
    
    private static int switchTagStr(String key) {
    	if(tagMap.containsKey(key))
    		return tagMap.get(key);
    	return 0;
	}
    
    private static int switchClassStr(String key) {
    	if(classMap.containsKey(key))
    		return classMap.get(key);
    	return 0;
	}

    private static int CalculateNodeScore(Element node)
    {
        int score = 0;
        switch(switchTagStr(node.nodeName()))
        {
            case 1:
                score += 5;
                break;

            case 2:
            case 3:
            case 4:
                score += 3;
                break;

            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                score -= 3;
                break;

            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
                score -= 5;
                break;
        }

        return score + GetClassWeight(node);
    }

    private static int GetClassWeight(Element node)
    {
        if (!node.hasAttr("class"))
            return 0;

        int score = 0;
        switch(switchClassStr(node.attr("class").toLowerCase()))
        {
            //"and|article|body|column|main|shadow|content"
            case 1:
            case 2:
            case 3:
            case 4:
                score += 10;
                break;
            case 5:
                score += 5;
                break;
            case 6:
            case 7:
            case 8:                
                score += 3;
                break;                
        }
        return score;
    }

//    private static String GetArticleTitle(Element htmlNode)
//    {        
//        if (htmlNode.getElementsByTag("title") == null) 
//        	return null;
//        
//        Element titleNode = htmlNode.getElementsByTag("title").get(0);
//
//        String currTitle, origTitle;
//        currTitle = origTitle = GetInnerText(titleNode);
//
//        if (Regex.IsMatch(currTitle, @" [\|\-] "))
//        {
//            currTitle = Regex.Replace(origTitle,  @"(.*)[\|\-] .*", "$1");
//
//            if (currTitle.Split(' ').Length < 3)
//            {
//                currTitle = origTitle.Replace(@"[^\|\-]*[\|\-](.*)", "$1");
//            }
//        }
//        else if (currTitle.IndexOf(": ") != -1)
//        {
//            currTitle = Regex.Replace(origTitle, @".*:(.*)", "$1");
//
//            if(currTitle.Split(' ').Length < 3)
//            {
//                currTitle = Regex.Replace(origTitle, @"[^:]*[:](.*)", "$1");
//            }
//        }
//        else if (currTitle.Length > 150 || currTitle.Length < 15)
//        {
//            var hOnes = htmlNode.GetElementsByTagName("h1");
//            if (hOnes.Count == 1)
//            {
//                currTitle = GetInnerText(hOnes[0]);
//            }
//        }
//
//        if (currTitle.Split(' ').Length <= 4)
//        {
//            currTitle = origTitle;
//        }
//    
//        return currTitle.Trim();
//    }

    private static String GetArticleContent(Document doc)
    {
        Element body = doc.body();

        List<Element> allElements = body.getAllElements();

        List<Element> nodesToScore = new ArrayList<Element>();

        for (int nodeIndex = 0, len = allElements.size(); nodeIndex < len; nodeIndex++){
            Element node = allElements.get(nodeIndex);
            String unlikelyMatchString = node.hasAttr("class")? node.attr("class"): "" + node.attr("id");
        	if (s_unlikelyCandidates.matcher(unlikelyMatchString).find() &&
                !s_okMaybeItsACandidate.matcher(unlikelyMatchString).find() &&
                node.nodeName() != "body" &&
                node.nodeName() != "html" &&
                node.nodeName() != "head")
            {
                node.remove();
                continue;
            }

            if (node.nodeName() == "p" || node.nodeName() == "td" || node.nodeName() == "pre")
            {
                nodesToScore.add(node);
            }

            if (node.nodeName() == "div")
            {
                if (!s_divToPElements.matcher(node.html()).find())
                {
                	if(node.ownerDocument() != null){
                		Element newNode = node.ownerDocument().createElement("p");
                        newNode.html(node.html());
                        node.replaceWith(newNode);
                        nodesToScore.add(newNode);
                	}
                }
                else
                {
                    for(Node childNode : node.childNodes())
                    {
                        if (childNode instanceof TextNode)
                        {
                        	if(node.ownerDocument() != null){
	                        	Element p = node.ownerDocument().createElement("p");
	                            p.html(((TextNode) childNode).text());
	                            childNode.replaceWith(p);	                            
                        	}
                        }                            
                    }
                }
            }
        }

        Map<Element, Integer> scores = new HashMap<Element, Integer>();

        List<Element> candidates = new ArrayList<Element>();
        for (int pt = 0, len = nodesToScore.size(); pt < len; pt++)
        {
            Element parentNode = nodesToScore.get(pt).parent();
            Element grandParentNode = parentNode != null ? parentNode.parent() : null;
            String innerText = GetInnerText(nodesToScore.get(pt));

            if (parentNode == null) continue;
            
            if (parentNode.nodeName() == "body") continue;
            
            if (parentNode.nodeName() == "html") continue;
            
            if (parentNode != null && parentNode.hasAttr("class") && parentNode.attr("class").equals("copyright")) continue;

            if (innerText.length() < 25) continue;

            if (!scores.containsKey(parentNode))
            {
                scores.put(parentNode, CalculateNodeScore(parentNode));
                candidates.add(parentNode);
            }

            if (grandParentNode != null && !scores.containsKey(grandParentNode))
            {
                scores.put(grandParentNode, CalculateNodeScore(grandParentNode));
                candidates.add(grandParentNode);
            }

            int contentScore = 0;

            contentScore++;            
            
            //for embed flash case
            if(innerText.contains("embed") && 
        		(
	            	innerText.contains("youku") ||
	            	innerText.contains("tudou") ||
	            	innerText.contains("ku6") ||
	            	innerText.contains("sohu") ||
	            	innerText.contains("weiphone") ||
	            	innerText.contains("56") || 
	            	innerText.contains("youtube") ||
	            	innerText.contains("qq")
        		))
            	contentScore += 50;
            	
            contentScore += innerText.split("[,]|[\uFF0C]").length;

            contentScore += Math.min(innerText.length() / 100, 3);

            int v = scores.get(parentNode);
            v += contentScore;
            scores.put(parentNode, v);

            if (grandParentNode != null)
            {
            	v = scores.get(grandParentNode);
            	v += contentScore / 2;
            	scores.put(grandParentNode, v);
            }
        }

        Element topCandidate = null;
        for(Element cand : candidates){
        	int v = scores.get(cand);
        	        	
            v = (int)(v * (1 - GetLinkDensity(cand)));
            
            scores.put(cand, v);

            if (topCandidate == null || scores.get(cand) > scores.get(topCandidate))
            {
                topCandidate = cand;
            }

            if (topCandidate == null || topCandidate.nodeName() == "body")
            {
                topCandidate = doc.createElement("div");
                topCandidate.html(body.html());
                body.html("");
                body.appendChild(topCandidate);
                scores.put(topCandidate, CalculateNodeScore(topCandidate));
            }
        }

        return topCandidate == null ? null : topCandidate.html();
    }
}

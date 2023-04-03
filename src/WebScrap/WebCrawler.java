package WebScrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WebCrawler {
	private static final int MAX_DEPTH = 10;
	private Set<String> visitedUrls = new HashSet<>();

	// ExecutorService responsável por gerenciar as threads utilizadas pelo robô
	private ExecutorService threadPool = Executors.newFixedThreadPool(10);

	public static void main(String[] args) throws Exception {
	    WebCrawler webCrawler = new WebCrawler();
	    webCrawler.startCrawling("https://pt.wikipedia.org/wiki/Clube_de_Regatas_do_Flamengo", 0);
	}

	private void startCrawling(String url, int depth) {
	    if (depth > MAX_DEPTH) {
	        return;
	    }
	    visitedUrls.add(url);

	    // Cria uma nova thread para realizar a busca por links na página
	    Runnable task = () -> {
	        try {
	            String pageContent = downloadPageContent(url);
	            System.out.println("Visitando " + url);
	            Set<String> links = extractLinks(pageContent);
	            for (String link : links) {
	                if (!visitedUrls.contains(link)) {
	                    // Se o link não foi visitado, inicia uma nova busca na página
	                    startCrawling(link, depth + 1);
	                }
	            }
	            // Após buscar novos links, faz a busca por palavras-chave na página atual
	            if (searchForKeyword(pageContent, "Flamengo")) {
	                System.out.println("Palavra-chave encontrada em " + url);
	            }
	        } catch (IOException e) {
	            System.out.println("Erro ao visitar " + url);
	        }
	    };

	    // Adiciona a nova thread ao pool de threads
	    threadPool.submit(task);
	}

	private String downloadPageContent(String url) throws IOException {
	    URL webpage = new URL(url);
	    BufferedReader in = new BufferedReader(new InputStreamReader(webpage.openStream()));

	    StringBuilder pageContentBuilder = new StringBuilder();
	    String inputLine;
	    while ((inputLine = in.readLine()) != null) {
	        pageContentBuilder.append(inputLine);
	    }
	    in.close();

	    return pageContentBuilder.toString();
	}

	private Set<String> extractLinks(String htmlContent) throws IOException {
	    Set<String> links = new HashSet<>();
	    Document doc = Jsoup.parse(htmlContent);
	    Elements linkElements = doc.select("a[href]");

	    Pattern pattern = Pattern.compile("^(https?|ftp)://[^\s/$.?#].[^\s]*$");

	    for (int i = 0; i < linkElements.size(); i++) {
	        String linkHref = linkElements.get(i).attr("href");
	        Matcher matcher = pattern.matcher(linkHref);
	        if (matcher.matches()) {
	            links.add(linkHref);
	        }
	    }

	    return links;
	}

	private boolean searchForKeyword(String htmlContent, String keyword) {
	    Document doc = Jsoup.parse(htmlContent);
	    String text = doc.body().text();
	    return text.contains(keyword);
	}

}
package de.libutzki.playwright;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.testcontainers.Testcontainers.exposeHostPorts;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.ScreenshotOptions;
import com.microsoft.playwright.Playwright;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class BookStoreYouTubeWT {

	@LocalServerPort
	private Integer port;

	private static final Path screenshotPath = Paths.get("target", "playwright");

	@Container
	public static GenericContainer<?> chromeContainer = new GenericContainer<>(
			DockerImageName.parse("browserless/chrome:latest"))
					.withAccessToHost(true)
					.withExposedPorts(3000)
					.waitingFor(Wait.forHttp("/"));

	@Test
	public void shouldDisplayBook() {
		exposeHostPorts(port);
		try (Playwright playwright = Playwright.create()) {
			Browser browser = playwright.chromium().connect(
					"ws://" + chromeContainer.getHost() + ":" + chromeContainer.getFirstMappedPort() + "/playwright");
			String baseUrl = String.format("http://host.testcontainers.internal:%d", port);
			try (Page page = browser.newPage()) {
				page.navigate(baseUrl + "/book-store");
				assertThat(page.locator("id=all-books")).not().isVisible();
				page.screenshot(new ScreenshotOptions().setPath(screenshotPath.resolve("pre-book-fetch.png")));
				page.locator("id=fetch-books").click();
				assertThat(page.locator("id=all-books")).isVisible();
				page.screenshot(new ScreenshotOptions().setPath(screenshotPath.resolve("post-book-fetch.png")));
			}
		}
	}
}

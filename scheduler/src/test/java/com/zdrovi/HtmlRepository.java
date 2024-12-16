package com.zdrovi;

public class HtmlRepository {

    static String getExpectedMessage(String title,
                                     String greeting,
                                     String content,
                                     String signature,
                                     String unsubscribeUrl) {
        return """
                <div style="font-family: Verdana, sans-serif;margin: 0;padding: 0;background-color: #f4f4f4;">
                                  <div class="container" style="width: 100%%;max-width: 600px;margin: 0 auto;background: linear-gradient(190deg, #87CEEB 20%%, #FDF8EA 60%%, #7CFC00 90%%);padding: 20px;box-shadow: 0 0 50px rgba(0, 0, 0, 0.3);overflow: hidden;">
                                      <div class="header" style="font-weight: bold;font-size: 24px;text-align: center;padding: 10px 0;color: #222222;">
                                          %s
                                      </div>
                                      <div class="greeting" style="font-weight: bold;font-size: 18px;padding: 20px 20px 0 20px;line-height: 1.6;color: #222222;">
                                          %s,
                                      </div>
                                      <div class="content" style="padding: 20px;text-align: justify;color: #222222;">
                                          %s
                                      </div>
                                      <div class="signature" style="font-size: 18px;margin: 0;padding: 0 20px 20px 20px;color: #222222;">
                                      %s,<br>Zespół Zdrovi
                                      </div>
                                      <div class="footer" style="text-align: center;padding: 10px 0;font-size: 12px;color: #777777;">
                                          <p>Nie chcesz otrzymywać więcej wiadomości? <a href="%s">Wypisz się</a></p>
                                          <p>&copy; 2025 Zdrovi. Wszelkie prawa zastrzeżone.</p>
                                      </div>
                                  </div>
                              </div>
                """.formatted(
                title,
                greeting,
                content,
                signature,
                unsubscribeUrl);
    }
}

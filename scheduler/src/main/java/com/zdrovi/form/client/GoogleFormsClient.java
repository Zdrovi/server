package com.zdrovi.form.client;

import com.zdrovi.google.api.V1Api;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "googleFormsClient", url = "${form.forms-url}")
public interface GoogleFormsClient extends V1Api {
}

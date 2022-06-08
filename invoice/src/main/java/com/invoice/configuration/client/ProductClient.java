package com.invoice.configuration.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.DtoProduct;

@FeignClient(name = "product-service")
public interface ProductClient {

	@GetMapping("product/{gtin}")
	public ResponseEntity<DtoProduct> getProduct(@PathVariable("gtin") String gtin);
	
	@PutMapping("product/update-stock")
	public ResponseEntity<ApiResponse> updateProductStock(List<DtoProduct> in);

}

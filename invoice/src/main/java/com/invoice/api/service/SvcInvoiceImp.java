package com.invoice.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.invoice.api.dto.ApiResponse;
import com.invoice.api.dto.DtoCustomer;
import com.invoice.api.dto.DtoProduct;
import com.invoice.api.entity.Cart;
import com.invoice.api.entity.Invoice;
import com.invoice.api.entity.Item;
import com.invoice.api.repository.RepoInvoice;
import com.invoice.api.repository.RepoItem;
import com.invoice.configuration.client.CustomerClient;
import com.invoice.configuration.client.ProductClient;
import com.invoice.exception.ApiException;

@Service
public class SvcInvoiceImp implements SvcInvoice {

	@Autowired
	RepoInvoice repo;
	
	@Autowired
	RepoItem repoItem;

	@Autowired
	SvcCart svcCart;
	
	@Autowired
	CustomerClient customerCl;
	
	@Autowired
	ProductClient productCl;
	
	@Override
	public List<Invoice> getInvoices(String rfc) {
		return repo.findByRfcAndStatus(rfc, 1);
	}

	@Override
	public List<Item> getInvoiceItems(Integer invoice_id) {
		return repoItem.getInvoiceItems(invoice_id);
	}

	@Override
	public ApiResponse generateInvoice(String rfc) {
		
		if (!validateCustomer(rfc))
			throw new ApiException(HttpStatus.BAD_REQUEST, "customer does not exist");
		
		List<Cart> carrito = svcCart.getCart(rfc);
		if (carrito == null)
			throw new ApiException(HttpStatus.NOT_FOUND, "cart has no items");
		
		// Invoice con atributos vacíos, lo necesitamos crear antes para obtener su id.
		Invoice invoice = new Invoice();
		invoice.setRfc(rfc);
		invoice.setSubtotal(0.0);
		invoice.setTaxes(0.0);
		invoice.setTotal(0.0);
		invoice.setCreated_at(LocalDateTime.now());
		invoice.setStatus(1);
		repo.save(invoice);
		
		// Este invoice ya tiene id
		invoice = repo.findByRfcAndTotal(rfc, 0.0);
		
		double totalIn = 0.0;
		double taxesIn = 0.0;
		List<DtoProduct> dtos = new ArrayList<DtoProduct>();
		
		for (Cart cart : carrito) {
			
			String gtin = cart.getGtin();
			ResponseEntity<DtoProduct> response = productCl.getProduct(gtin);
			
			int quantity = cart.getQuantity();
			double price = response.getBody().getPrice();
			double total = quantity * price;
			double taxes = total * 0.16;
			
			Item nuevo   = new Item();
			nuevo.setId_invoice(invoice.getInvoice_id());
			nuevo.setGtin(gtin);
			nuevo.setQuantity(quantity);
			nuevo.setUnit_price(price);
			nuevo.setTaxes(taxes);
			nuevo.setSubtotal(total - taxes);
			nuevo.setTotal(total);
			nuevo.setStatus(1);
			
			repoItem.save(nuevo);
			totalIn += total;
			taxesIn += taxes;
			
			DtoProduct dto = new DtoProduct();
			dto.setGtin(gtin);
			dto.setStock(quantity);
			dto.setPrice(price);
			dtos.add(dto);
		}
		
		invoice.setTotal(totalIn);
		invoice.setTaxes(taxesIn);
		invoice.setSubtotal(totalIn - taxesIn);
		invoice.setCreated_at(LocalDateTime.now());
		repo.save(invoice);
		
		productCl.updateProductStock(dtos);
		
		// Falta vaciar el carrito
		
		return new ApiResponse("invoice generated");
	}
	
	private boolean validateCustomer(String rfc) {
		try {
			ResponseEntity<DtoCustomer> response = customerCl.getCustomer(rfc);
			if (response.getStatusCode() == HttpStatus.OK)
				return true;
			else
				return false;
		} catch (Exception e) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "unable to retrieve customer information");
		}
	}

}

package com.product.api.service;

import java.util.List;

import com.product.api.dto.ApiResponse;
import com.product.api.dto.DtoProductList;
import com.product.api.entity.Category;
import com.product.api.entity.Product;

public interface SvcProduct {

	List<DtoProductList> getProducts(Integer category_id);

	Product getProduct(String gtin);

	ApiResponse updateProductCategory(Category category, Integer id);

	ApiResponse createProduct(Product in);

	ApiResponse updateProduct(Product in, Integer id);

	ApiResponse deleteProduct(Integer id);

	ApiResponse updateProductStock(List<Product> in);

}

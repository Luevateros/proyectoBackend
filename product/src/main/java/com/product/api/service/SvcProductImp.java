package com.product.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.product.api.entity.Product;
import com.product.api.dto.ApiResponse;
import com.product.api.dto.DtoProductList;
import com.product.api.entity.Category;
import com.product.api.repository.RepoCategory;
import com.product.api.repository.RepoProduct;
import com.product.api.repository.RepoProductList;
import com.product.exception.ApiException;

@Service
public class SvcProductImp implements SvcProduct {

	@Autowired
	RepoProduct repo;

	@Autowired
	RepoProductList repoProductList;

	@Autowired
	RepoCategory repoCategory;

	@Override
	public List<DtoProductList> getProducts(Integer category_id) {
		return repoProductList.findByCategoryId(category_id);
	}

	@Override
	public Product getProduct(String gtin) {
		Product product = repo.findByGtinAndStatus(gtin, 1);
		if (product == null)
			throw new ApiException(HttpStatus.NOT_FOUND, "product does not exist");
		product.setCategory(repoCategory.findByCategoryId(product.getCategory_id()));
		return product;
	}

	@Override
	public ApiResponse createProduct(Product in) {
		Product product = repo.findByGtinAndStatus(in.getGtin(), 0);
		if (product == null) {
			try {
				in.setStatus(1);
				repo.save(in);
			} catch (DataIntegrityViolationException e) {
				if (e.getLocalizedMessage().contains("gtin"))
					throw new ApiException(HttpStatus.BAD_REQUEST, "product gtin already exists");
				if (e.getLocalizedMessage().contains("product"))
					throw new ApiException(HttpStatus.BAD_REQUEST, "product name already exists");
			}
			return new ApiResponse("product created");
		}
		updateProduct(in, product.getProduct_id());
		return new ApiResponse("product activated");
	}

	@Override
	public ApiResponse updateProduct(Product in, Integer id) {
		getProduct(in.getGtin());// Si no existe el producto mandamos API Excpetion
		try {
			repo.updateProduct(	id, 
								in.getGtin(), 
								in.getProduct(), 
								in.getDescription(), 
								in.getPrice(), 
								in.getStock());
		} catch (DataIntegrityViolationException e) {
			if (e.getLocalizedMessage().contains("gtin"))
				throw new ApiException(HttpStatus.BAD_REQUEST, "product gtin already exists");
			if (e.getLocalizedMessage().contains("product"))
				throw new ApiException(HttpStatus.BAD_REQUEST, "product name already exists");
		}
		return new ApiResponse("product updated");
	}

	@Override
	public ApiResponse deleteProduct(Integer id) {
		if (repo.deleteProduct(id) == 0)
			throw new ApiException(HttpStatus.BAD_REQUEST, "product cannot be deleted");
		return new ApiResponse("product removed");
	}

	@Override
	public ApiResponse updateProductCategory(Category category, Integer id) {
		try {
			if (repo.updateProductCategory(category.getCategory_id(), id) > 0)
				return new ApiResponse("product category updated");
			throw new ApiException(HttpStatus.BAD_REQUEST, "product category cannot be update");
		} catch (DataIntegrityViolationException e) {
			throw new ApiException(HttpStatus.NOT_FOUND, "category not found");
		}
	}

	@Override
	public ApiResponse updateProductStock(List<Product> in) {
		for (Product product : in) {
			Product p = getProduct(product.getGtin());// Si no existe el producto mandamos API Excpetion
			if (p.getStock() - product.getStock() < 0)
				throw new ApiException(HttpStatus.BAD_REQUEST, "product stock not valid");
			repo.updateProductStock(product.getProduct_id(), product.getStock());
		}
		return new ApiResponse("products stock updated");
	}

}

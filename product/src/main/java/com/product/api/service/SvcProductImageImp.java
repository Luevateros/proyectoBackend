package com.product.api.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.product.api.dto.ApiResponse;
import com.product.api.entity.ProductImage;
import com.product.api.repository.RepoProductImage;
import com.product.exception.ApiException;

@Service
@PropertySource("classpath:configuration/path.config")
public class SvcProductImageImp implements SvcProductImage {

	@Autowired
	RepoProductImage repo;
	
	@Value("${product.images.path}")
	private String path;
	
	@Override
	public List<ProductImage> getProductImages(Integer product_id) {
		return repo.findByProductId(product_id);
	}

	@Override
	public ApiResponse createProductImage(ProductImage in) {
		try {
			File folder = new File(path + "/" + in.getProduct_id());
			if(!folder.exists())
				folder.mkdirs();
			String file = path + "/" + in.getProduct_id() + "/img_" + new Date().getTime() + ".bmp";
			byte[] data = Base64.getMimeDecoder().decode(in.getImage().substring(in.getImage().indexOf(",")+1, in.getImage().length()));
			try(OutputStream stream = new FileOutputStream(file)){
				stream.write(data);
			}
			in.setStatus(1);
			in.setImage(in.getProduct_id() + "/img_" + new Date().getTime() + ".bmp");
			repo.save(in);
			return new ApiResponse("product image created");
		} catch (Exception e) { // DataIntegrityViolationException
			throw new ApiException(HttpStatus.BAD_REQUEST, "product image cannot be created" + ". " + e.getLocalizedMessage());
		}
		
	}

	@Override
	public ApiResponse deleteProductImage(Integer id) {
		if (repo.deleteProductImage(id) == 0)
			throw new ApiException(HttpStatus.BAD_REQUEST, "product image cannot be deleted");
		return new ApiResponse("product image removed");
	}

}

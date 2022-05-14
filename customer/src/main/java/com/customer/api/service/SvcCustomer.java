package com.customer.api.service;

import java.util.List;

import com.customer.api.dto.ApiResponse;
import com.customer.api.dto.DtoCustomerList;
import com.customer.api.entity.Customer;
import com.customer.api.entity.Region;

public interface SvcCustomer {

	List<DtoCustomerList> getCustomers();
	Customer getCustomer(String rfc);
	ApiResponse createCustomer(Customer in);
	ApiResponse updateCustomer(Customer in, Integer id);
	ApiResponse deleteCustomer(Integer id);
	ApiResponse updateCustomerRegion(Region region, Integer id);
}

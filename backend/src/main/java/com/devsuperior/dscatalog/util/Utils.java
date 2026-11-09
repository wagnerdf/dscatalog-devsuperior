package com.devsuperior.dscatalog.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.projections.ProductProjection;

public class Utils {
	
	public static List<Product> replace(List<ProductProjection> ordered, List<Product> unordered){
		
		Map<Long, Product> map = new HashMap<>();
		for(Product obj : unordered) {
			map.put(obj.getId(), obj);
		}
		
		List<Product> result = new ArrayList<>();
		for (ProductProjection obj : ordered) {
			result.add(map.get(obj.getId()));
		}
		
		return result;
	}

}


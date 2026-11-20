package com.devsuperior.dscatalog.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.devsuperior.dscatalog.projections.IdProjection;

/**
 * Classe utilitária que disponibiliza métodos auxiliares
 * para manipulação de listas e projeções.
 */
public class Utils {
	
	/**
	 * Reorganiza os elementos de uma lista de acordo com a ordem
	 * dos identificadores presentes em outra lista.
	 * 
	 * Utiliza um Map para localizar os elementos pelo ID,
	 * permitindo reconstruir a lista na ordem desejada.
	 *
	 * @param ordered lista que define a ordem desejada dos elementos
	 * @param unordered lista contendo os elementos que serão reorganizados
	 * @param <ID> tipo do identificador utilizado pelas projeções
	 * @return lista de elementos reorganizada conforme a ordem da lista ordered
	 */
	public static <ID> List<? extends IdProjection<ID>> replace(List<? extends IdProjection<ID>> ordered, List<? extends IdProjection<ID>> unordered){
		
		Map<ID, IdProjection<ID>> map = new HashMap<>();
		for(IdProjection<ID> obj : unordered) {
			map.put(obj.getId(), obj);
		}
		
		List<IdProjection<ID>> result = new ArrayList<>();
		for (IdProjection<ID> obj : ordered) {
			result.add(map.get(obj.getId()));
		}
		
		return result;
	}

}


package com.example.actor.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service // サービスクラスに付与。
public class RandomNumberService {
	// 整数0-9の乱数を返却。
	public int zeroToNine() {
		return (int) (Math.random() * 10);
	}

	public List<Integer> random(Integer x) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < x; i++) {
			list.add(zeroToNine());
		}
		return list;
	}
}

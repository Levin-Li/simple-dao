package com.levin.commons.dao.proxy;

@API
public interface CustomerService {

  String findNameById(Long id);

  void freezeById(Long id);

}

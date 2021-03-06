/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Business;

import Entities.Customer;
import Entities.Sale;
import Entities.SaleItem;
import Repository.SaleItemRepository;
import Utilities.OperationPackage;
import Repository.SaleRepository;
import java.time.LocalDate;

/**
 *
 * @author lucas.budelon
 */
public class SaleBusiness implements IBusiness<Sale> {

    private final SaleRepository _repository;

    public SaleBusiness() {
        _repository = new SaleRepository();
    }

    /*
     * Returns all records
     * @author Lucas B Budelon
     * @return ArrayList
     */
    @Override
    public OperationPackage GetAll() {
        return _repository.SearchAll();
    }

    @Override
    public OperationPackage Get(int id) {
        return _repository.SearchByPK(id);
    }

    @Override
    public OperationPackage Get(String code) {
        return _repository.SearchByAK(code);
    }

    @Override
    public OperationPackage Insert(Sale model) {

        CustomerBusiness customerBusiness = new CustomerBusiness();

        OperationPackage result = customerBusiness.Get(model.getCustomer().getCpf());

        if (result.ValidOperation) {

            model.setCustomer((Customer) result.Data);

            if (model.getTotal() > model.getCustomer().getAccount().getBalance()) {
                result = new OperationPackage("Não é possível realizar venda pois o cliente não possui saldo suficiente!", false);
            } else {

                String code = Utilities.Tools.GenerateRandomString(6);

                model.setCode(code);
                model.setDate(LocalDate.now());

                result = _repository.Insert(model);

                if (result.ValidOperation) {

                    OperationPackage resultItemsSale = _repository.SearchByAK(code);

                    if (resultItemsSale.ValidOperation) {

                        int saleId = ((Sale) resultItemsSale.Data).getId();

                        SaleItemRepository saleItemRepository = new SaleItemRepository();
                        AccountBusiness accountBusiness = new AccountBusiness();

                        for (SaleItem saleItem : model.items) {

                            resultItemsSale = saleItemRepository.Insert(saleId, saleItem);

                            if (resultItemsSale.ValidOperation) {

                                resultItemsSale = accountBusiness
                                        .Withdrawal(model.getCustomer().getCpf(), saleItem.getProduct().getPrice() * saleItem.getQuantity());

                                if (!resultItemsSale.ValidOperation) {
                                    return resultItemsSale;
                                }
                            } else {
                                return resultItemsSale;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public OperationPackage Update(Sale model) {
        return new OperationPackage("Não é possivel atualizar", false);
    }

    @Override
    public OperationPackage Delete(int id) {
        return new OperationPackage("Não é possivel excluir por Id", false);
    }

    @Override
    public OperationPackage Delete(String code) {
        return new OperationPackage("Não é possivel excluir pelo Código", false);
    }
}

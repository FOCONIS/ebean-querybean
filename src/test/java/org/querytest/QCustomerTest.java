package org.querytest;

import io.ebean.PagedList;
import io.ebean.Query;
import io.ebean.QueryIterator;
import io.ebean.annotation.Transactional;
import org.example.domain.Customer;
import org.example.domain.typequery.QContact;
import org.example.domain.typequery.QCustomer;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class QCustomerTest {

  @Test
  public void findSingleAttribute() {

    List<String> names = new QCustomer()
        .setDistinct(true)
        .select(QCustomer.alias().name)
        .status.equalTo(Customer.Status.BAD)
        .findSingleAttributeList();

    assertThat(names).isNotNull();
  }

  @Test
  public void findIterate() {

    Customer cust = new Customer();
    cust.setName("foo");
    cust.setStatus(Customer.Status.GOOD);
    cust.save();

    List<Long> ids = new QCustomer()
        .status.equalTo(Customer.Status.GOOD)
        .findIds();

    assertThat(ids).isNotEmpty();


    Map<List, Customer> map = new QCustomer()
        .status.equalTo(Customer.Status.GOOD)
        .findMap();

    assertThat(map.size()).isEqualTo(ids.size());

    QueryIterator<Customer> iterate = new QCustomer()
        .status.equalTo(Customer.Status.GOOD)
        .findIterate();

    try {
      while (iterate.hasNext()) {
        Customer customer = iterate.next();
        customer.getName();
      }
    } finally {
      iterate.close();
    }
  }


  @Test
  public void isEmpty() {

    new QCustomer()
        .contacts.isEmpty()
        .findList();

    new QCustomer()
        .contacts.isNotEmpty()
        .findList();
  }

  @Transactional
  @Test
  public void forUpdate() {

    new QCustomer()
        .id.eq(42)
        .forUpdate()
        .findOne();

    new QCustomer()
        .id.eq(42)
        .forUpdateNoWait()
        .findOne();

    new QCustomer()
        .id.eq(42)
        .forUpdateSkipLocked()
        .findOne();
  }


  @Ignore
  @Test
  public void arrayContains() {

    new QContact()
        .phoneNumbers.contains("4312")
        .findList();

    new QCustomer()
        .contacts.phoneNumbers.contains("4312")
        .findList();
  }

  @Test
  public void setIncludeSoftDeletes() {

    new QCustomer()
        .setIdIn(42L)
        .setIncludeSoftDeletes()
        .findList();
  }

  @Test
  public void testIdIn() {

    new QCustomer()
        .setIdIn("1", "2")
        .findList();

    new QCustomer()
        .id.in(1L, 2L, 3L)
        .findList();
  }

  @Test
  public void testIn() {
    new QCustomer()
        .id.in(34L, 33L)
        .name.in("asd", "foo", "bar")
        .registered.in(new Date())
        .findList();
  }

  @Test
  public void testNotIn() {
    new QCustomer()
        .id.isIn(34L, 33L)
        .name.notIn("asd", "foo", "bar")
        .registered.in(new Date())
        .findList();
  }

  @Test
  public void testQueryBoolean() {

    new QCustomer()
        .name.contains("rob")
        //.setUseDocStore(true)
        .setMaxRows(10)
        .findPagedList();

    new QCustomer()
        .inactive.isFalse()
        .findList();
  }

  @Test
  public void testFindOne() {

    new QCustomer()
      .name.isIn("rob", "foo")
      //.setUseDocStore(true)
      .setMaxRows(1)
      .findOne();

    Optional<Customer> maybe = new QCustomer()
      .name.contains("rob")
      //.setUseDocStore(true)
      .setMaxRows(1)
      .findOneOrEmpty();

    maybe.isPresent();

    new QCustomer()
      .inactive.isFalse()
      .findList();
  }

  @Test
  public void testDate_lessThan() {

    assertContains(new QCustomer().registered.lt(new Date()).query(), " where t0.registered < ?");
    assertContains(new QCustomer().registered.before(new Date()).query(), " where t0.registered < ?");
    assertContains(new QCustomer().registered.lessThan(new Date()).query(), " where t0.registered < ?");
  }

  @Test
  public void testDate_lessOrEqualTo() {

    assertContains(new QCustomer().registered.le(new Date()).query(), " where t0.registered <= ?");
    assertContains(new QCustomer().registered.lessOrEqualTo(new Date()).query(), " where t0.registered <= ?");
  }

  @Test
  public void testDate_greaterThan() {

    assertContains(new QCustomer().registered.after(new Date()).query(), " where t0.registered > ?");
    assertContains(new QCustomer().registered.gt(new Date()).query(), " where t0.registered > ?");
    assertContains(new QCustomer().registered.greaterThan(new Date()).query(), " where t0.registered > ?");
  }


  @Test
  public void testDate_greaterOrEqualTo() {

    assertContains(new QCustomer().registered.ge(new Date()).query(), " where t0.registered >= ?");
    assertContains(new QCustomer().registered.greaterOrEqualTo(new Date()).query(), " where t0.registered >= ?");
  }

  private void assertContains(Query<Customer> query, String match) {
    query.findList();
    assertThat(query.getGeneratedSql()).contains(match);
  }

  @Test
  public void testQuery() {

    QContact contact = QContact.alias();
    QCustomer cust = QCustomer.alias();

    new QCustomer()
        // tune query
        .select(cust.name)
        .status.isIn(Customer.Status.BAD, Customer.Status.BAD)
        .contacts.fetch()
        // predicates
        .findList();

    new QCustomer()
        // tune query
        .select(cust.name)
        .contacts.fetch()
        // predicates
        .findList();

    new QCustomer()
        // tune query
        .select(cust.id, cust.name)
        .contacts.fetch(contact.firstName, contact.lastName, contact.email)
        // predicates
        .id.greaterThan(1)
        .findList();

    PagedList<Customer> pagedList = new QCustomer()
        // tune query
        .select(cust.id, cust.name)
        .contacts.fetch(contact.firstName, contact.lastName, contact.email)
        // predicates
        .id.greaterThan(1)
        .setFirstRow(20)
        .setMaxRows(10)
        .findPagedList();

    pagedList.getList();
    pagedList.getList();

//    new QCustomer()
//        .asDraft()
//        .findList();
//
//    new QCustomer()
//        .includeSoftDeletes()
//        .findList();

//    List<Contact> contacts
//        = new QContact()
//        .email.like("asd")
//        .notes.title.like("asd")
//        .orderBy()
//          .id.desc()
//        .findList();
//

//    List<Customer> customers = new QCustomer()
//        .id.eq(1234)
//        .status.equalTo(Customer.Status.BAD)
//        .status.in(Customer.Status.GOOD, Customer.Status.MIDDLING)
//            //.status.eq(Order.Status.APPROVED)
//        .name.like("asd")
//        .name.istartsWith("ASdf")
//        .registered.after(new Date())
//        .contacts.email.endsWith("@foo.com")
//        .contacts.notes.id.greaterThan(123L)
//        .orderBy().id.asc()
//        .findList();

//    //Customer customer3 =
//    new QCustomer()
//        .id.gt(12)
//        .or()
//          .id.lt(1234)
//          .and()
//            .name.like("one")
//            .name.like("two")
//          .endAnd()
//        .endOr()
//        .orderBy().id.asc()
//        .findList();
//
//    //where t0.id > ?  and (t0.id < ?  or (t0.name like ?  and t0.name like ? ) )  order by t0.id; --bind(12,1234,one,two)
//
////    List<Customer> customers
////        = new QCustomer()
////          .name.like("asd")
////          .findList();
//
//    Customer.find.where()
//        .gt("id", 1234)
//        .disjunction().eq("id", 1234).like("name", "asd")
//        .endJunction().findList();

//    QCustomer cust = QCustomer.I;
//    ExpressionList<Customer> expr = new QCustomer().expr();
//    expr.eq(cust.contacts.email, 123);
  }
}

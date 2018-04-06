/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';

/**
 * This is a mixin that can be added UserContext object of a Component test suite for {@link
 * domain.centres.Shipment Shipments}.
 *
 * @exports test.mixins.ShippingComponentTestSuiteMixin
 */
let ShippingComponentTestSuiteMixin = {

  /**
   * Used to inject AngularJS dependencies into the test suite.
   *
   * Also injects dependencies required by this mixin.
   *
   * @param {...string} dependencies - the AngularJS dependencies to inject.
   *
   * @return {undefined}
   */
  injectDependencies: function (...dependencies) {
    const allDependencies = dependencies.concat([
      '$q',
      'Shipment',
      'ShipmentSpecimen',
      'Factory']);
    ComponentTestSuiteMixin.injectDependencies.call(this, ...allDependencies);
  },

  /**
   * Creates a {@link domain.centres.Shipment Shipment}.
   *
   * @param {object} options - properties for the shipment.
   *
   * @return {domain.centres.Shipment}
   */
  createShipment: function (options = {}) {
    return this.Shipment.create(this.Factory.shipment(options));
  },

  /**
   * Creates a {@link domain.centres.Shipment Shipment} with {@link domain.participants.Specimen Specimens}.
   *
   * @param {number} specimenCount - the number of specimens this shipment contains.
   *
   * @return {domain.centres.Shipment}
   */
  createShipmentWithSpecimens: function (specimenCount) {
    return ShippingComponentTestSuiteMixin.createShipment.call(this, { specimenCount: specimenCount });
  },

  /**
   * Creates a Jasmine spy on the {@link domain.centres.Shipment#get Shipment.get()} method.
   *
   * @param {domain.centres.Shipment} shipment - the shipment to be returned by the spy.
   */
  createGetShipmentSpy: function (shipment) {
    this.Shipment.get = jasmine.createSpy().and.returnValue(this.$q.when(shipment));
  },

  /**
   * Creates a Jasmine spy on the {@link domain.centres.ShipmentSpecimen#list ShipmentSpecimen.list()}
   * method.
   *
   * @param {domain.centres.ShipmentSpecimen} shipmentSpecimen - the *shipment specimen* to be returned
   * by the spy in a {@link common.controllers.PagedListController.PagedResult PagedResult}.
   */
  createShipmentSpecimensListSpy: function (shipmentSpecimens) {
    var reply = this.Factory.pagedResult(shipmentSpecimens);
    this.ShipmentSpecimen.list = jasmine.createSpy().and.returnValue(this.$q.when(reply));
  }

}

ShippingComponentTestSuiteMixin = Object.assign({},
                                                ComponentTestSuiteMixin,
                                                ShippingComponentTestSuiteMixin);

export { ShippingComponentTestSuiteMixin };
export default () => {};

/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash'

describe('shipmentViewComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test')
    angular.mock.inject(function(ShippingComponentTestSuiteMixin) {
      _.extend(this, ShippingComponentTestSuiteMixin)

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Shipment',
                              'ShipmentState',
                              'Factory')
      this.createController = function (shipment) {
        ShippingComponentTestSuiteMixin.createController.call(
          this,
          '<shipment-view shipment="vm.shipment"></shipment-view>',
          { shipment: shipment },
          'shipmentView')
      }
    })
  })

  it('has valid header for shipment state', function() {
    var shipment,
        headerByState = {}

    headerByState[this.ShipmentState.PACKED]    = 'Packed shipment'
    headerByState[this.ShipmentState.SENT]      = 'Sent shipment'
    headerByState[this.ShipmentState.RECEIVED]  = 'Received shipment'
    headerByState[this.ShipmentState.COMPLETED] = 'Completed shipment'
    headerByState[this.ShipmentState.LOST]      = 'Lost shipment'

    Object.keys(headerByState).forEach((state) => {
      shipment = this.createShipment({ state: state })
      this.createController(shipment)
      console.log(`"${state}", "${headerByState[state]}", "${this.controller.pageHeader}"`)
      expect(this.controller.pageHeader).toBe(headerByState[state])
    })
  })

})

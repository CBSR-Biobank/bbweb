/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../index'

describe('shipmentsIncomingComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test')
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin)

      this.injectDependencies('$rootScope',
                              'Centre',
                              'Factory')

      this.centre = this.Centre.create(this.Factory.centre())

      this.createController = (centre = this.centre) =>
        ComponentTestSuiteMixin.createController.call(
          this,
          '<shipments-incoming centre="vm.centre"></shipments-incoming>',
          { centre },
          'shipmentsIncoming');
    })
  })

  it('has valid scope', function() {
    this.createController()
    expect(this.controller.centre).toBe(this.centre)
  })

  it('emits `tabbed-page-update` event when created', function() {
    let eventEmitted = false;

    this.$rootScope.$on('tabbed-page-update', function (event, arg) {
      expect(arg).toBe('tab-selected');
      eventEmitted = true;
    });

    this.createController();
    expect(eventEmitted).toBeTrue();
  });

})

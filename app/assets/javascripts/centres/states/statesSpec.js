/**
 * Jasmine test suite
 *
 */
/* global angular */

import ngModule from '../index'

describe('states', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test', function ($qProvider) {
      // this is needed to make promis rejections work for a state's resolves
      $qProvider.errorOnUnhandledRejections(false);
    })

    angular.mock.inject(function(StateTestSuiteMixin) {
      Object.assign(this, StateTestSuiteMixin)

      this.injectDependencies('$q',
                              '$rootScope',
                              '$location',
                              '$httpBackend',
                              '$state',
                              'Centre',
                              'Shipment')
    })
  })

  it('when navigating to `/shipping` should go to valid state', function () {
    this.gotoUrl('/shipping')
    expect(this.$state.current.name).toBe('home.shipping')
  })

  describe('when navigating to `/shipping/centres/{centreId}/incoming`', function () {
    const context = {}

    beforeEach(function() {
      context.childState = 'incoming'
    });

    shippingCentreCommonBehaviour(context)
  })

  describe('when navigating to `/shipping/centres/{centreId}/outgoing`', function () {
    const context = {}

    beforeEach(function() {
      context.childState = 'outgoing'
    });

    shippingCentreCommonBehaviour(context)
  })

  describe('when navigating to `/shipping/centres/{centreId}/completed`', function () {
    const context = {}

    beforeEach(function() {
      context.childState = 'completed'
    });

    shippingCentreCommonBehaviour(context)
  })

  it('when navigating to `/shipping/add`', function() {
    this.gotoUrl('/shipping/add')
    expect(this.$state.current.name).toBe('home.shipping.add')
  })

  describe('when navigating to `/shipping/additems/{shipmentId}`', function() {

    it('should go to valid state', function() {
      this.Shipment.get = jasmine.createSpy().and.returnValue(this.$q.when({ test: 'test'}))
      this.gotoUrl('/shipping/additems/shipment-id-1')
      expect(this.$state.current.name).toBe('home.shipping.addItems')
    })

    it('should go to the 404 state when an invalid shipmentId is used', function() {
      this.Shipment.get = jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
      this.gotoUrl('/shipping/additems/shipment-1')
      expect(this.$state.current.name).toBe('404')
    })

  })

  describe('when navigating to `/shipping/{shipmentId}`', function() {

    it('should go to valid state', function() {
      this.Shipment.get = jasmine.createSpy().and.returnValue(this.$q.when({ test: 'test'}))
      this.gotoUrl('/shipping/shipment-id-1')
      expect(this.$state.current.name).toBe('home.shipping.shipment')
    })

    it('should go to the 404 state when an invalid shipmentId is used', function() {
      this.Shipment.get = jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
      this.gotoUrl('/shipping/shipment-id-1')
      expect(this.$state.current.name).toBe('404')
    })

  })

  describe('when navigating to `home.shipping.shipment.unpack` child states', function() {

    beforeEach(function() {
      this.Shipment.get = jasmine.createSpy().and.returnValue(this.$q.when({ test: 'test'}))
    });

    it('when navigating to `/shipping/{shipmentId}/unpack/information`', function() {
      this.gotoUrl('/shipping/shipment-id-1/unpack/information')
      expect(this.$state.current.name).toBe('home.shipping.shipment.unpack.info')
    })

    it('when navigating to `/shipping/{shipmentId}/unpack/received`', function() {
      this.gotoUrl('/shipping/shipment-id-1/unpack/received')
      expect(this.$state.current.name).toBe('home.shipping.shipment.unpack.received')
    })

    it('when navigating to `/shipping/{shipmentId}/unpack/missing`', function() {
      this.gotoUrl('/shipping/shipment-id-1/unpack/missing')
      expect(this.$state.current.name).toBe('home.shipping.shipment.unpack.missing')
    })

    it('when navigating to `/shipping/{shipmentId}/unpack/extra`', function() {
      this.gotoUrl('/shipping/shipment-id-1/unpack/extra')
      expect(this.$state.current.name).toBe('home.shipping.shipment.unpack.extra')
    })

  })

  function shippingCentreCommonBehaviour(context) {

    describe('(shared)', function() {

      it(`should go to the centre's ${context.childState} shipment state`, function() {
        this.Centre.get = jasmine.createSpy().and.returnValue(this.$q.when({ test: 'test'}))
        this.gotoUrl(`/shipping/centres/centre-id-1/${context.childState}`)
        expect(this.$state.current.name).toBe(`home.shipping.centre.${context.childState}`)
      })

      it('should go to the 404 state when an invalid centreId is used', function() {
        this.Centre.get = jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
        this.gotoUrl('/shipping/centres/centre-id-1/outgoing')
        expect(this.$state.current.name).toBe('404')
      })

    })
  }

})

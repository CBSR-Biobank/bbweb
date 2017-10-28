/**
 * Jasmine test suite
 *
 */
/* global angular */

describe('states', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test')
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

  describe('when navigating to `/shipping`', function () {

    it('should go to the shipping state', function() {
      this.gotoUrl('/shipping')
      expect(this.$state.current.name).toBe('home.shipping')
    })

  })

  describe('when navigating to `/shipping/centres/{centreId}/incoming`', function () {

    fit('should go to the centre`s incoming shipments state', function() {
      this.Centre.get = jasmine.createSpy().and.returnValue(this.$q.when({}))
      this.gotoUrl('/shipping/centres/centre-id-1/incoming')
      expect(this.$state.current.name).toBe('home.shipping.centre.incoming')
    })

    fit('should go to the 404 state when an invalid centreId is used', function() {
      this.Centre.get = jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
      this.gotoUrl('/shipping/centres/centre-id-1/incoming')
      this.$rootScope.$digest()
      expect(this.$state.current.name).toBe('404')
    })

  })

})

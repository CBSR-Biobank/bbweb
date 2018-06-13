/**
 * Jasmine test suite
 *
 */
/* global angular */

import { StateTestSuiteMixin } from 'test/mixins/StateTestSuiteMixin';
import ngModule from '../../../../app' // the whole appliction has to be loaded for these states

describe('admin/centre states', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test', function ($qProvider) {
      // this is needed to make promise rejections work for a state's resolves
      $qProvider.errorOnUnhandledRejections(false);
    })

    angular.mock.inject(function() {
      Object.assign(this, StateTestSuiteMixin)

      this.injectDependencies('$q',
                              '$rootScope',
                              '$location',
                              '$httpBackend',
                              '$state',
                              'Centre',
                              'Factory')

      this.init();
      this.initAuthentication()
    })
  })

  it('when navigating to `/admin/centres` should go to valid state', function () {
    this.gotoUrl('/admin/centres')
    expect(this.$state.current.name).toBe('home.admin.centres')
  })

  it('when navigating to `/admin/centres/add` should go to valid state', function () {
    this.gotoUrl('/admin/centres/add')
    expect(this.$state.current.name).toBe('home.admin.centres.add')
  })

  describe('when navigating to `/admin/centres/centre-id-1`/summary', function () {
    const context = {}

    beforeEach(function() {
      context.childState = 'summary'
    });

    centreStateSharedBehaviour(context)

  })

  describe('when navigating to `/admin/centres/centre-id-1/locations`', function () {
    const context = {}

    beforeEach(function() {
      context.childState = 'locations'
    });

    centreStateSharedBehaviour(context)

  })

  it('when navigating to `/admin/centres/centre-id-1/location/add` should go to valid state', function () {
    this.Centre.get = jasmine.createSpy().and.returnValue(this.$q.when({ test: 'test'}))
    this.gotoUrl('/admin/centres/centre-id-1/locations/add')
    expect(this.$state.current.name).toBe('home.admin.centres.centre.locations.locationAdd')
  })

  describe('when navigating to `/admin/centres/centre-id-1/locations/view/location-id-1`', function () {

    it('should go to valid state', function () {
      this.Centre.get = jasmine.createSpy().and.returnValue(this.$q.when({
        locations: [
          { id: 'location-id-1', slug:  'location-id-1' }
        ]
      }))
      this.gotoUrl('/admin/centres/centre-id-1/locations/view/location-id-1')
      expect(this.$state.current.name).toBe('home.admin.centres.centre.locations.locationView')
    })

    it('should go to the 404 state when an invalid locationId is used', function() {
      this.Centre.get = jasmine.createSpy().and.returnValue(this.$q.when({
        locations: []
      }))
      this.gotoUrl('/admin/centres/centre-id-1/locations/view/location-id-1')
      expect(this.$state.current.name).toBe('404')
    })

  })

  it('when navigating to `/admin/centres/centre-id-1/studies` should go to valid state', function () {
    this.Centre.get = jasmine.createSpy().and.returnValue(this.$q.when({ test: 'test'}))
    this.gotoUrl('/admin/centres/centre-id-1/studies')
    expect(this.$state.current.name).toBe('home.admin.centres.centre.studies')
  })

  function centreStateSharedBehaviour(context) {

    describe('(shared)', function() {

    it('should go to valid state', function () {
      this.Centre.get = jasmine.createSpy().and.returnValue(this.$q.when({ test: 'test'}))
      this.gotoUrl(`/admin/centres/centre-id-1/${context.childState}`)
      expect(this.$state.current.name).toBe(`home.admin.centres.centre.${context.childState}`)
    })

    it('should go to the 404 state when an invalid centreId is used', function() {
      this.Centre.get = jasmine.createSpy().and.returnValue(this.$q.reject('simulated error'))
      this.gotoUrl(`/admin/centres/centre-id-1/${context.childState}`)
      expect(this.$state.current.name).toBe('404')
    })

    })

  }


})

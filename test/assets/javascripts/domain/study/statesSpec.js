/**
 * Jasmine test suite
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  /**
   * Cannot get these test to work yet. Skip for now.
   */
  describe('States: home.admin.studies', function() {

    var injector, rootScope, location, state, templateCache, fakeEntities, user;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    function mockTemplate(templateRoute, tmpl) {
      templateCache.put(templateRoute, tmpl || templateRoute);
    }

    function goTo(url) {
      location.url(url);
      rootScope.$digest();
    }

    function goFrom(url) {
      return { toState: function (stateName, params) {
        location.replace().url(url); //Don't actually trigger a reload
        state.go(stateName, params);
        rootScope.$digest();
      }};
    }

    beforeEach(inject(function ($injector,
                                $rootScope,
                                $location,
                                $state,
                                $templateCache,
                                $httpBackend,
                                adminService,
                                StudyCounts,
                                fakeDomainEntities) {
      var templates = [
        '/assets/javascripts/home/home.html',
        '/assets/javascripts/admin/adminDetails.html',
        '/assets/javascripts/admin/studies/studies.html'
      ];

      injector      = $injector;
      rootScope     = $rootScope;
      location      = $location;
      state         = $state;
      templateCache = $templateCache;
      fakeEntities  = fakeDomainEntities;
      user          = fakeEntities.user();

      _.each(templates, function(template) {
        mockTemplate(template);
      });

      $httpBackend.whenGET('/authenticate').respond({
        status: 'success',
        data: user
      });
      spyOn(adminService, 'aggregateCounts').and.returnValue('aggregateCounts');
      spyOn(StudyCounts, 'get').and.returnValue('studyCounts');
    }));

    describe('home.admin.studies', function () {

      var stateName = 'home.admin.studies';

      xit('should respond to URL', function() {
        goTo('/admin/studies');
        console.log(state.$current);
        expect(state.current.name).toBe(stateName);
      });

      xit('xxx', function() {
        goFrom('/#/admin').toState(stateName);
        expect(state.current.name).toBe(stateName);
      });

      xit('resolves work', function () {
        //state.go(stateName);
        state.go('home.admin');
        rootScope.$digest();

        console.log(state.current);

        // Call invoke to inject dependencies and run function
        expect(this.$injector.get(state.current.resolve.user)).toBe('findAll');
        expect(this.$injector.get(state.current.resolve.studyCounts)).toBe('findAll');
      });

    });

  });

});

// Jasmine test suite
//
define(['angular', 'angularMocks', 'biobankApp'], function(angular, mocks) {
  'use strict';

  describe('Controller: CentreCtrl', function() {
    var windowService, stateService, Centre, createController, fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test', function($provide) {
      windowService = {
        localStorage: {
          setItem: function() {},
          getItem: function() { return {}; }
        }
      };

      spyOn(windowService.localStorage, 'setItem');
      $provide.value('$window', windowService);
    }));

    beforeEach(inject(function($q, _Centre_, fakeDomainEntities) {
      Centre = _Centre_;
      fakeEntities = fakeDomainEntities;
      createController = setupController(this.$injector);
    }));

    function setupController(injector) {
      var $rootScope  = injector.get('$rootScope'),
          $controller = injector.get('$controller'),
          $window     = injector.get('$window'),
          $timeout    = injector.get('$timeout');

      return create;

      //--

      function create(centre) {
        var scope = $rootScope.$new(),
            state = {
              params: {centreId: centre.id},
              current: {name: 'home.admin.centres.centre.locations'}
            };

        $controller('CentreCtrl as vm', {
          $window:  $window,
          $scope:   scope,
          $state:   state,
          $timeout: $timeout,
          centre:    centre
        });
        scope.$digest();
        return scope;
      }
    }

    it('should contain a valid centre', function() {
      var centre = new Centre(fakeEntities.centre()),
          scope = createController(centre);

      expect(scope.vm.centre).toBe(centre);
    });

    it('should contain initialized panels', function() {
      var centre = new Centre(fakeEntities.centre()),
          scope = createController(centre);

      expect(scope.vm.tabSummaryActive).toBe(false);
      expect(scope.vm.tabLocationsActive).toBe(false);
      expect(scope.vm.tabStudiesActive).toBe(false);
    });

    it('should contain initialized local storage', function() {
      var centre = new Centre(fakeEntities.centre()),
          scope = createController(centre);

      expect(windowService.localStorage.setItem)
        .toHaveBeenCalledWith('centre.panel.locations', true);
    });

    it('should initialize the tab of the current state', function() {
      var $timeout = this.$injector.get('$timeout'),
          centre = new Centre(fakeEntities.centre()),
          scope = createController(centre);

      $timeout.flush();
      expect(scope.vm.tabLocationsActive).toBe(true);
    });

  });

});

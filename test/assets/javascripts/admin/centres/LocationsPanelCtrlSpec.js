/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, testUtils) {
  'use strict';

  describe('Controller: LocationsPanelCtrl', function() {
        var createEntities, createController, fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(fakeDomainEntities) {
      fakeEntities = fakeDomainEntities;
      createEntities = setupEntities(this.$injector);
      createController = setupController(this.$injector);
      testUtils.addCustomMatchers();
    }));

    function setupEntities(injector) {
      var Centre = injector.get('Centre'),
          Location = injector.get('Location');

      return create;

      //---

      function create() {
        var entities = {};
        entities.centre = new Centre(fakeEntities.centre());
        entities.locations = _.map(_.range(3), function () {
          return new Location(fakeEntities.location());
        });
        return entities;
      }

    };

    function setupController(injector) {
      var $rootScope          = injector.get('$rootScope'),
          $controller         = injector.get('$controller'),
          $state              = injector.get('$state'),
          LocationViewer      = injector.get('LocationViewer'),
          Panel               = injector.get('Panel'),
          domainEntityService = injector.get('domainEntityService'),
          tableService        = injector.get('tableService');

      return create;

      //--

      function create(centre) {
        var scope = $rootScope.$new();

        scope.centre = centre;

        $controller('LocationsPanelCtrl as vm', {
          $scope:              scope,
          $state:              $state,
          LocationViewer:      LocationViewer,
          Panel:               Panel,
          domainEntityService: domainEntityService,
          tableService:        tableService
        });
        scope.$digest();
        return scope;
      }
    }

    it('can add a location', function() {
      var $state = this.$injector.get('$state'),
          entities = createEntities(),
          scope = createController(entities.centre);

      spyOn($state, 'go').and.callFake(function () {});
      scope.vm.add();
      scope.$digest();
      expect($state.go).toHaveBeenCalledWith('home.admin.centres.centre.locationAdd');
    });

    it('can view location information', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          entities = createEntities(),
          locationToView = entities.locations[0],
          scope;

      entities.centre.locations.push(locationToView);
      scope = createController(entities.centre);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () {});
      scope.vm.information(locationToView);
      scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('can update a location', function() {
      var $state = this.$injector.get('$state'),
          entities = createEntities(),
          locationToUpdate = entities.locations[0],
          scope;

      entities.centre.locations.push(locationToUpdate);
      scope = createController(entities.centre);

      spyOn($state, 'go').and.callFake(function () {});
      scope.vm.update(locationToUpdate);
      scope.$digest();
      expect($state.go).toHaveBeenCalledWith('home.admin.centres.centre.locationUpdate',
                                             { locationId: locationToUpdate.id});
    });

    it('can remove a location', function() {
      var $q                  = this.$injector.get('$q'),
          modalService        = this.$injector.get('modalService'),
          entities            = createEntities(),
          locationToRemove    = entities.locations[0],
          scope;

      entities.centre.locations.push(locationToRemove);
      scope = createController(entities.centre);

       spyOn(modalService, 'showModal').and.callFake(function () {
        return $q.when('OK');
       });
      spyOn(entities.centre, 'removeLocation').and.callFake(function () {
        return $q.when(entities.centre);
      });
      spyOn(scope.vm.tableParams, 'reload').and.callFake(function () {});
      scope.vm.remove(locationToRemove);
      scope.$digest();
      expect(entities.centre.removeLocation).toHaveBeenCalledWith(locationToRemove);
    });

    it('displays information modal when removal of a location fails', function() {
      var $q                  = this.$injector.get('$q'),
          modalService        = this.$injector.get('modalService'),
          entities            = createEntities(),
          locationToRemove    = entities.locations[0],
          scope;

      entities.centre.locations.push(locationToRemove);
      scope = createController(entities.centre);

       spyOn(modalService, 'showModal').and.callFake(function () {
        return $q.when('OK');
       });
      spyOn(entities.centre, 'removeLocation').and.callFake(function () {
        var deferred = $q.defer();
        deferred.reject('error');
        return deferred.promise;
      });
      spyOn(scope.vm.tableParams, 'reload').and.callFake(function () {});
      scope.vm.remove(locationToRemove);
      scope.$digest();
      expect(modalService.showModal.calls.count()).toBe(2);
    });

  });

});

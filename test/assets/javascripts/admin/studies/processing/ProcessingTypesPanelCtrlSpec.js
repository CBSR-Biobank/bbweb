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

  describe('Controller: ProcessingTypesPanelCtrl', function() {
    var createEntities,
        createController;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function () {
      createEntities = setupEntities(this.$injector);
      createController = setupController(this.$injector);
      testUtils.addCustomMatchers();
    }));

    function setupEntities(injector) {
      var Study          = injector.get('Study'),
          ProcessingType = injector.get('ProcessingType'),
          fakeEntities   = injector.get('fakeDomainEntities');

      return create;

      //--

      function create() {
        var entities = {};
        entities.study = new Study(fakeEntities.study());
        entities.processingTypes = _.map(_.range(2), function () {
          return new ProcessingType(fakeEntities.processingType(entities.study));
        });
        return entities;
      }
    }

    function setupController(injector) {
      var rootScope            = injector.get('$rootScope'),
          controller           = injector.get('$controller'),
          state                = injector.get('$state'),
          Panel                = injector.get('Panel'),
          tableService         = injector.get('tableService'),
          ProcessingTypeViewer = injector.get('ProcessingTypeViewer'),
          domainEntityService  = injector.get('domainEntityService');

      return create;

      //--

      function create(study, processingTypes) {
        var scope = rootScope.$new();

        scope.study = study;
        scope.processingTypes = processingTypes;

        controller('ProcessingTypesPanelCtrl as vm', {
          $scope:               scope,
          $state:               state,
          Panel:                Panel,
          tableService:         tableService,
          ProcessingTypeViewer: ProcessingTypeViewer,
          domainEntityService:  domainEntityService
        });
        scope.$digest();
        return scope;
      }
    }

    it('has valid scope', function () {
      var entities = createEntities(),
          scope = createController(entities.study,
                                   entities.processingTypes);

      expect(scope.vm.study).toBe(entities.study);
      expect(scope.vm.processingTypes).toBeArrayOfSize(entities.processingTypes.length);
      expect(scope.vm.processingTypes).toContainAll(entities.processingTypes);
      expect(scope.vm.tableParams).toBeDefined();
    });

    it('can add a processing type', function() {
      var state = this.$injector.get('$state'),
          entities = createEntities(),
          scope = createController(entities.study,
                                   entities.processingTypes);

      spyOn(state, 'go').and.callFake(function () {});
      scope.vm.add();
      scope.$digest();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.processing.processingTypeAdd');
    });

    it('can view information for a processing type', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          entities = createEntities(),
          scope = createController(entities.study,
                                   entities.processingTypes);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
      scope.vm.information(entities.processingTypes[0]);
      scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('cannot update a processing type if study is not disabled', function() {
      var StudyStatus = this.$injector.get('StudyStatus'),
          entities = createEntities(),
          statuses = [StudyStatus.ENABLED(), StudyStatus.RETIRED()],
          scope;

      _.each(statuses, function (status) {
        entities.study.status = status;
        scope = createController(entities.study, entities.processingTypes);
        expect(function () { scope.vm.update(entities.processingTypes[0]); }).
          toThrow(new Error('study is not disabled'));
      });
    });

    it('can update a processing type', function() {
      var state = this.$injector.get('$state'),
          entities = createEntities(),
          scope = createController(entities.study, entities.processingTypes),
          processingType = entities.processingTypes[0];

      spyOn(state, 'go').and.callFake(function () {});
      scope.vm.update(processingType);
      scope.$digest();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.processing.processingTypeUpdate',
        { processingTypeId: processingType.id });
    });

    it('cannot update a processing type if study is not disabled', function() {
      var StudyStatus = this.$injector.get('StudyStatus'),
          entities = createEntities(),
          statuses = [StudyStatus.ENABLED(), StudyStatus.RETIRED()],
          scope;

      _.each(statuses, function (status) {
        entities.study.status = status;
        scope = createController(entities.study, entities.processingTypes);
        expect(function () { scope.vm.remove(entities.processingTypes[0]); }).
          toThrow(new Error('study is not disabled'));
      });
    });

    /**
     * A spy is needed on scope.vm.tableParams due to a bug in ng-table.
     */
    it('can remove a processing type', function() {
      var q                   = this.$injector.get('$q'),
          domainEntityService = this.$injector.get('domainEntityService'),
          entities            = createEntities(),
          scope               = createController(entities.study, entities.processingTypes),
          ptToRemove          = entities.processingTypes[1];

      spyOn(domainEntityService, 'removeEntity').and.callFake(function () {
        return q.when('OK');
      });
      spyOn(scope.vm.tableParams, 'reload').and.callFake(function () {});
      scope.vm.remove(ptToRemove);
      scope.$digest();
      expect(domainEntityService.removeEntity).toHaveBeenCalled();
      expect(scope.vm.processingTypes).toBeArrayOfSize(entities.processingTypes.length - 1);
    });

    /**
     * A spy is needed on scope.vm.tableParams due to a bug in ng-table.
     */
    it('displays a modal if removal of a processing type fails', function() {
      var q             = this.$injector.get('$q'),
          modalService  = this.$injector.get('modalService'),
          entities      = createEntities(),
          scope         = createController(entities.study, entities.processingTypes),
          ptToRemove    = entities.processingTypes[1];

      spyOn(ptToRemove, 'remove').and.callFake(function () {
        var deferred = q.defer();
        deferred.reject('error');
        return deferred.promise;
      });
      spyOn(modalService, 'showModal').and.callFake(function () {
        return q.when('OK');
      });
      spyOn(scope.vm.tableParams, 'reload').and.callFake(function () {});

      scope.vm.remove(ptToRemove);
      scope.$digest();
      expect(modalService.showModal.calls.count()).toBe(2);
    });


  });

});

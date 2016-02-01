/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, testUtils) {
  'use strict';

  describe('Controller: SpecimenGroupsPanelCtrl', function() {

    var createEntities,
        createController;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function ($injector) {
      createEntities = setupEntities($injector);
      createController = setupController($injector);
      testUtils.addCustomMatchers();
    }));

    function setupEntities(injector) {
      var Study         = injector.get('Study'),
          SpecimenGroup = injector.get('SpecimenGroup'),
          fakeEntities  = injector.get('fakeDomainEntities');

      return create;

      //--

      function create() {
        var entities = {};

        entities.study = new Study(fakeEntities.study());
        entities.specimenGroups = _.map(_.range(2), function () {
          return new SpecimenGroup(fakeEntities.processingType(entities.study));
        });
        entities.specimenGroupIdsInUse = [ entities.specimenGroups[0].id ];

        return entities;
      }
    }

    function setupController(injector) {
      var rootScope           = injector.get('$rootScope'),
          controller          = injector.get('$controller'),
          state               = injector.get('$state'),
          Panel               = injector.get('Panel'),
          modalService        = injector.get('modalService'),
          domainEntityService = injector.get('domainEntityService'),
          SpecimenGroupViewer = injector.get('SpecimenGroupViewer'),
          specimenGroupUtils  = injector.get('specimenGroupUtils');

      return create;

      //--

      function create(entities) {
        var scope = rootScope.$new();

        scope.study                 = entities.study;
        scope.specimenGroups        = entities.specimenGroups;
        scope.specimenGroupIdsInUse = entities.specimenGroupIdsInUse;

        controller('SpecimenGroupsPanelCtrl as vm', {
          $scope:              scope,
          $state:              state,
          Panel:               Panel,
          modalService:        modalService,
          domainEntityService: domainEntityService,
          SpecimenGroupViewer: SpecimenGroupViewer,
          specimenGroupUtils:  specimenGroupUtils
        });
        scope.$digest();
        return scope;
      }
    }

    it('has valid scope', function () {
      var entities = createEntities(),
          scope = createController(entities);
      expect(scope.vm.study).toBe(entities.study);
      expect(scope.vm.specimenGroups).toBeArrayOfSize(entities.specimenGroups.length);
      expect(scope.vm.specimenGroups).toContainAll(entities.specimenGroups);

      expect(scope.vm.specimenGroupIdsInUse).toBeArrayOfSize(entities.specimenGroupIdsInUse.length);
      expect(scope.vm.specimenGroupIdsInUse).toContainAll(entities.specimenGroupIdsInUse);
    });

    it('can add specimen group', function() {
      var state    = this.$injector.get('$state'),
          entities = createEntities(),
          scope    = createController(entities);

      spyOn(state, 'go').and.callFake(function () {});
      scope.vm.add();
      scope.$digest();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.specimens.groupAdd');
    });

    it('can view information for a specimen group', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          entities     = createEntities(),
          scope        = createController(entities);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
      scope.vm.information(entities.specimenGroups[0]);
      scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('cannot update a specimen group if study is not disabled', function() {
      var StudyStatus = this.$injector.get('StudyStatus'),
          entities    = createEntities(),
          scope       = createController(entities),
          statuses    = [StudyStatus.ENABLED(), StudyStatus.RETIRED()];

      _.each(statuses, function (status) {
        scope.study.status = status;

        expect(function () { scope.vm.update(entities.specimenGroups[0]); }).
          toThrow(new Error('study is not disabled'));
      });
    });

    it('cannot update a specimen group if study is in use', function() {
      var modalService = this.$injector.get('modalService'),
          entities    = createEntities(),
          scope       = createController(entities);

      spyOn(modalService, 'modalOk').and.callFake(function () {});
      scope.vm.update(entities.specimenGroups[0]);
      expect(modalService.modalOk).toHaveBeenCalled();
    });

    it('can update a specimen group', function() {
      var state    = this.$injector.get('$state'),
          entities = createEntities(),
          scope    = createController(entities),
          specimenGroup = entities.specimenGroups[1];

      spyOn(state, 'go').and.callFake(function () {});
      scope.vm.update(specimenGroup);
      scope.$digest();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.specimens.groupUpdate',
        { specimenGroupId: specimenGroup.id });
    });

    it('cannot remove a specimen group if study is not disabled', function() {
      var StudyStatus = this.$injector.get('StudyStatus'),
          entities    = createEntities(),
          scope       = createController(entities),
          statuses    = [StudyStatus.ENABLED(), StudyStatus.RETIRED()];

      _.each(statuses, function (status) {
        scope.study.status = status;

        expect(function () { scope.vm.remove(entities.specimenGroups[0]); }).
          toThrow(new Error('study is not disabled'));
      });
    });

    it('cannot update a specimen group if study is in use', function() {
      var modalService = this.$injector.get('modalService'),
          entities    = createEntities(),
          scope       = createController(entities);

      spyOn(modalService, 'modalOk').and.callFake(function () {});
      scope.vm.remove(entities.specimenGroups[0]);
      expect(modalService.modalOk).toHaveBeenCalled();
    });

    it('can remove a specimen group', function() {
      var q                   = this.$injector.get('$q'),
          domainEntityService = this.$injector.get('domainEntityService'),
          entities            = createEntities(),
          scope               = createController(entities),
          sgToRemove          = entities.specimenGroups[1];

      spyOn(domainEntityService, 'removeEntity').and.callFake(function () {
        return q.when('OK');
      });
      scope.vm.remove(sgToRemove);
      scope.$digest();
      expect(domainEntityService.removeEntity).toHaveBeenCalled();
      expect(scope.vm.specimenGroups).toBeArrayOfSize(entities.specimenGroups.length - 1);
    });

    it('displays a modal if removal of a specimen group fails', function() {
      var q                   = this.$injector.get('$q'),
          modalService        = this.$injector.get('modalService'),
          entities            = createEntities(),
          scope               = createController(entities),
          sgToRemove          = entities.specimenGroups[1];

      spyOn(sgToRemove, 'remove').and.callFake(function () {
        var deferred = q.defer();
        deferred.reject('error');
        return deferred.promise;
      });
      spyOn(modalService, 'showModal').and.callFake(function () {
        return q.when('OK');
      });

      scope.vm.remove(sgToRemove);
      scope.$digest();
      expect(modalService.showModal.calls.count()).toBe(2);
    });

  });

});

/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

xdescribe('Directive: specimenGroupsPanelDirective', function() {

  var scope,
      controller,
      createEntities,
      createController;

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function (TestSuiteMixin, testUtils) {
      _.extend(this, TestSuiteMixin.prototype);

      createEntities = setupEntities(this.$injector);
      createController = setupController(this.$injector);
      testUtils.addCustomMatchers();
    });
  });

  function setupEntities(injector) {
    var Study         = injector.get('Study'),
        SpecimenGroup = injector.get('SpecimenGroup'),
        factory  = injector.get('factory');

    return create;

    //--

    function create() {
      var entities = {};

      entities.study = new Study(factory.study());
      entities.specimenGroups = _.range(2).map(() => SpecimenGroup.create(factory.processingType(entities.study)));
      entities.specimenGroupIdsInUse = [ entities.specimenGroups[0].id ];

      return entities;
    }
  }

  function setupController(injector) {
    var rootScope = injector.get('$rootScope'),
        $compile   = injector.get('$compile');

    return create;

    //--

    function create(entities) {
      var element = angular.element([
        '<uib-accordion close-others="false">',
        '  <specimen-groups-panel',
        '     study="vm.study"',
        '     specimen-groups="vm.specimenGroups"',
        '     specimen-group-ids-in-use="vm.specimenGroupIdsInUse"></specimen-groups-panel>',
        '</uib-accordion>'
      ].join(''));

      scope = rootScope.$new();

      scope.vm = {
        study:                 entities.study,
        specimenGroups:        entities.specimenGroups,
        specimenGroupIdsInUse: entities.specimenGroupIdsInUse
      };

      $compile(element)(scope);
      scope.$digest();
      controller = element.find('specimen-groups-panel').controller('specimenGroupsPanel');
    }
  }

  it('has valid scope', function () {
    var entities = createEntities();
    createController(entities);
    expect(controller.study).toBe(entities.study);
    expect(controller.specimenGroups).toBeArrayOfSize(entities.specimenGroups.length);
    expect(controller.specimenGroups).toContainAll(entities.specimenGroups);

    expect(controller.specimenGroupIdsInUse).toBeArrayOfSize(entities.specimenGroupIdsInUse.length);
    expect(controller.specimenGroupIdsInUse).toContainAll(entities.specimenGroupIdsInUse);
  });

  it('can add specimen group', function() {
    var state    = this.$injector.get('$state'),
        entities = createEntities();

    createController(entities);
    spyOn(state, 'go').and.callFake(function () {});
    controller.add();
    scope.$digest();
    expect(state.go).toHaveBeenCalledWith(
      'home.admin.studies.study.specimens.groupAdd');
  });

  it('can view information for a specimen group', function() {
    var EntityViewer = this.$injector.get('EntityViewer'),
        entities     = createEntities();

    createController(entities);
    spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
    controller.information(entities.specimenGroups[0]);
    scope.$digest();
    expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
  });

  it('cannot update a specimen group if study is not disabled', function() {
    var StudyState = this.$injector.get('StudyState'),
        entities    = createEntities(),
        statuses    = [StudyState.ENABLED, StudyState.RETIRED];

    createController(entities);
    _.each(statuses, function (status) {
      entities.study.status = status;

      expect(function () { controller.update(entities.specimenGroups[0]); }).
        toThrow(new Error('study is not disabled'));
    });
  });

  it('cannot update a specimen group if study is in use', function() {
    var modalService = this.$injector.get('modalService'),
        entities    = createEntities();

    createController(entities);
    spyOn(modalService, 'modalOk').and.callFake(function () {});
    controller.update(entities.specimenGroups[0]);
    expect(modalService.modalOk).toHaveBeenCalled();
  });

  it('can update a specimen group', function() {
    var state    = this.$injector.get('$state'),
        entities = createEntities(),
        specimenGroup = entities.specimenGroups[1];

    createController(entities);
    spyOn(state, 'go').and.callFake(function () {});
    controller.update(specimenGroup);
    scope.$digest();
    expect(state.go).toHaveBeenCalledWith(
      'home.admin.studies.study.specimens.groupUpdate',
      { specimenGroupId: specimenGroup.id });
  });

  it('cannot remove a specimen group if study is not disabled', function() {
    var StudyState = this.$injector.get('StudyState'),
        entities    = createEntities(),
        statuses    = [StudyState.ENABLED, StudyState.RETIRED];

    createController(entities);
    _.each(statuses, function (status) {
      entities.study.status = status;

      expect(function () { controller.remove(entities.specimenGroups[0]); }).
        toThrow(new Error('study is not disabled'));
    });
  });

  it('cannot update a specimen group if study is in use', function() {
    var modalService = this.$injector.get('modalService'),
        entities    = createEntities();

    createController(entities);
    spyOn(modalService, 'modalOk').and.callFake(function () {});
    controller.remove(entities.specimenGroups[0]);
    expect(modalService.modalOk).toHaveBeenCalled();
  });

  it('can remove a specimen group', function() {
    var q                   = this.$injector.get('$q'),
        domainNotificationService = this.$injector.get('domainNotificationService'),
        entities            = createEntities(),
        sgToRemove          = entities.specimenGroups[1];

    createController(entities);
    spyOn(domainNotificationService, 'removeEntity').and.callFake(function () {
      return q.when('OK');
    });
    controller.remove(sgToRemove);
    scope.$digest();
    expect(domainNotificationService.removeEntity).toHaveBeenCalled();
    expect(controller.specimenGroups).toBeArrayOfSize(entities.specimenGroups.length - 1);
  });

  it('displays a modal if removal of a specimen group fails', function() {
    var q                   = this.$injector.get('$q'),
        modalService        = this.$injector.get('modalService'),
        entities            = createEntities(),
        sgToRemove          = entities.specimenGroups[1],
        deferred            = q.defer();

    createController(entities);
    spyOn(sgToRemove, 'remove').and.returnValue(deferred.promise);
    spyOn(modalService, 'showModal').and.callFake(function () {
      return q.when('OK');
    });
    deferred.reject('simulated error');

    controller.remove(sgToRemove);
    scope.$digest();
    expect(modalService.showModal.calls.count()).toBe(2);
  });

});

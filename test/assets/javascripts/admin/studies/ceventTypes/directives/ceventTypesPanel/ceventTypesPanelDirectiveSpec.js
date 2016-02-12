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
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: ceventTypesPanelDirective', function() {
    var scope,
        controller,
        createEntities,
        createController;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (testUtils) {
      createEntities   = setupEntities(this.$injector);
      createController = setupController(this.$injector);

      testUtils.addCustomMatchers();

      testUtils.putHtmlTemplates(
        '/assets/javascripts/admin/studies/ceventTypes/directives/ceventTypesPanel/ceventTypesPanel.html',
        '/assets/javascripts/common/directives/panelButtons.html',
        '/assets/javascripts/common/directives/updateRemoveButtons.html');
    }));

    function setupEntities(injector) {
      var Study                         = injector.get('Study'),
          CollectionEventType           = injector.get('CollectionEventType'),
          SpecimenGroup                 = injector.get('SpecimenGroup'),
          CollectionEventAnnotationType = injector.get('CollectionEventAnnotationType'),
          AnnotationValueType           = injector.get('AnnotationValueType'),
          fakeEntities                  = injector.get('fakeDomainEntities');

      return create;

      //--

      function create(options) {
        var entities = {};

        options = options || {
          studyHasSpecimenGroups: true,
          studyHasAnnotationTypes: true
        };

        entities.study = new Study(fakeEntities.study());

        if (options.studyHasSpecimenGroups) {
          entities.specimenGroups = _.map(_.range(2), function () {
            return new SpecimenGroup(fakeEntities.specimenGroup(entities.study));
          });
        } else {
          entities.specimenGroups = [];
        }

        if (options.studyHasAnnotationTypes) {
          entities.annotationTypes = _.map(
            AnnotationValueType.values(),
            function(valueType) {
              return new CollectionEventAnnotationType(
                fakeEntities.studyAnnotationType(
                  entities.study, { valueType: valueType }));
            });
          entities.annotationTypeIdsInUse = [entities.annotationTypes[0]];
        } else {
          entities.annotationTypes = [];
        }

        entities.ceventTypes = _.map(_.range(2), function () {
          var serverObj =
              fakeEntities.collectionEventType(entities.study, {
                specimenGroups: entities.specimenGroups,
                annotationTypes: entities.annotationTypes
              });
          return new CollectionEventType(serverObj, {
            studySpecimenGroups: entities.specimenGroups,
            studyAnnotationTypes: entities.annotationTypes
          });
        });

        return entities;
      }
    }

    function setupController(injector) {
      var $rootScope = injector.get('$rootScope'),
          $compile   = injector.get('$compile');

      return create;

      //--
      function create(entities) {
        var element;

        element = angular.element([
          '<uib-accordion close-others="false">',
          '  <cevent-types-panel',
          '     study="vm.study"',
          '     cevent-types="vm.ceventTypes"',
          '     annotation-types="vm.annotationTypes"',
          '     annotation-type-ids-in-use="vm.annotationTypeIdsInUse"',
          '     specimen-groups="vm.specimenGroups">',
          '  </cevent-types-panel>',
          '</uib-accordion>'
        ].join(''));

        scope = $rootScope.$new();
        scope.vm = {
          study:                  entities.study,
          ceventTypes:            entities.ceventTypes,
          annotationTypes:        entities.annotationTypes,
          annotationTypeIdsInUse: [],
          specimenGroups:         entities.specimenGroups
        };

        $compile(element)(scope);
        scope.$digest();
        controller = element.find('cevent-types-panel').controller('ceventTypesPanel');
      }
    }

    it('has valid scope', function () {
      var entities = createEntities();

      createController(entities);

      expect(controller.study).toBe(entities.study);
      _.each(scope.specimenGroups, function (sg) {
        expect(controller.specimenGroupsById[sg.id]).toBe(sg);
      });
      _.each(scope.annotationTypes, function (at) {
        expect(controller.annotationTypesById[at.id]).toBe(at);
      });
      expect(controller.ceventTypes).toBeArrayOfSize(entities.ceventTypes.length);
      expect(controller.ceventTypes).toContainAll(entities.ceventTypes);
    });

    it('cannot add a collection event type if study has no specimen groups', function() {
      var modalService = this.$injector.get('modalService'),
          entities = createEntities({ studyHasSpecimenGroups: false,
                                      studyHasAnnotationTypes: false
                                    });

      createController(entities);

      spyOn(modalService, 'modalOk').and.callFake(function () {});

      controller.add();
      scope.$digest();

      expect(modalService.modalOk).toHaveBeenCalled();
    });

    it('can add collection event type', function() {
      var state = this.$injector.get('$state'),
          entities = createEntities();

      createController(entities);

      spyOn(state, 'go').and.callFake(function () {});

      controller.add();
      scope.$digest();

      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection.ceventTypeAdd');
    });

    it('can view information for a collection event type', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          entities = createEntities();

      createController(entities);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
      controller.information(controller.ceventTypes[0]);
      scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('can view information for a specimen group', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          entities = createEntities();

      createController(entities);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
      controller.viewSpecimenGroup(entities.specimenGroups[0].id);
      scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('can view information for an annotation type', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          entities = createEntities();

      createController(entities);

      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
      controller.viewAnnotationType(entities.annotationTypes[0].id);
      scope.$digest();
      expect(EntityViewer.prototype.showModal).toHaveBeenCalled();
    });

    it('cannot update a collection event type if study is not disabled', function() {
      var StudyStatus = this.$injector.get('StudyStatus'),
          statuses = [StudyStatus.ENABLED(), StudyStatus.RETIRED()],
          entities = createEntities();

      createController(entities);

      _.each(statuses, function (status) {
        entities.study.status = status;

        expect(function () { controller.update(entities.ceventTypes[0]); }).
          toThrow(new Error('study is not disabled'));
      });
    });

    it('can update a collection event type', function() {
      var state = this.$injector.get('$state'),
          entities = createEntities();

      createController(entities);

      spyOn(state, 'go').and.callFake(function () {});

      controller.update(entities.ceventTypes[0]);
      scope.$digest();

      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.collection.ceventTypeUpdate',
        { ceventTypeId: entities.ceventTypes[0].id });
    });

    it('cannot remove a collection event type if study is not disabled', function() {
      var StudyStatus = this.$injector.get('StudyStatus'),
          statuses = [StudyStatus.ENABLED(), StudyStatus.RETIRED()],
          entities = createEntities();

      createController(entities);

      _.each(statuses, function (status) {
        entities.study.status = status;

        expect(function () { controller.remove(entities.ceventTypes[0]); }).
          toThrow(new Error('study is not disabled'));
      });
    });

    it('can remove a collection event type', function() {
      var q                   = this.$injector.get('$q'),
          domainEntityService = this.$injector.get('domainEntityService'),
          entities            = createEntities(),
          cetToRemove         = entities.ceventTypes[1];

      createController(entities);

      spyOn(domainEntityService, 'removeEntity').and.callFake(function () {
        return q.when('OK');
      });
      controller.remove(cetToRemove);
      scope.$digest();
      expect(domainEntityService.removeEntity).toHaveBeenCalled();
      expect(controller.ceventTypes).toBeArrayOfSize(entities.ceventTypes.length - 1);
    });

    it('displays a modal if removal of a collection event type fails', function() {
      var q                   = this.$injector.get('$q'),
          modalService        = this.$injector.get('modalService'),
          entities            = createEntities(),
          cetToRemove         = entities.ceventTypes[1];

      createController(entities);
      spyOn(cetToRemove, 'remove').and.callFake(function () {
        var deferred = q.defer();
        deferred.reject('error');
        return deferred.promise;
      });
      spyOn(modalService, 'showModal').and.callFake(function () {
        return q.when('OK');
      });

      controller.remove(cetToRemove);
      scope.$digest();
      expect(modalService.showModal.calls.count()).toBe(2);
    });

  });

});

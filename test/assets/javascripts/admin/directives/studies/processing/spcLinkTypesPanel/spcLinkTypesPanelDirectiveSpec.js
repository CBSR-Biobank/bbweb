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

  xdescribe('Directive: spcLinkTypesPanelDirective', function() {
    var scope,
        controller,
        createEntities,
        createController,
        jsonEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (directiveTestSuite, testUtils) {
      _.extend(this, directiveTestSuite);

      jsonEntities = this.$injector.get('jsonEntities');

      createEntities = setupEntities(this.$injector);
      createController = setupController(this.$injector);
      testUtils.addCustomMatchers();

      this.putHtmlTemplates(
        '/assets/javascripts/admin/directives/studies/processing/spcLinkTypesPanel/spcLinkTypesPanel.html',
        '/assets/javascripts/common/directives/panelButtons.html',
        '/assets/javascripts/common/directives/infoUpdateRemoveButtons.html');
    }));

    function setupEntities(injector) {
      var Study                      = injector.get('Study'),
          ProcessingType             = injector.get('ProcessingType'),
          SpecimenLinkAnnotationType = injector.get('SpecimenLinkAnnotationType'),
          AnnotationValueType        = injector.get('AnnotationValueType'),
          SpecimenLinkType           = injector.get('SpecimenLinkType'),
          jsonEntities               = injector.get('jsonEntities');

      return create;

      //--

      function create(options) {
        var entities = {};

        options = options || {
          studyHasSpecimenGroups: true,
          studyHasAnnotationTypes: true
        };

        entities.study = new Study(jsonEntities.study());
        entities.processingTypes = _.map(_.range(2), function () {
          return new ProcessingType(jsonEntities.processingType(entities.study));
        });

        if (options.studyHasSpecimenGroups) {
          entities.specimenGroups = _.map(_.range(2), function () {
            return jsonEntities.specimenGroup(entities.study);
          });
        } else {
          entities.specimenGroups = [];
        }

        if (options.studyHasAnnotationTypes) {
          entities.annotationTypes = _.map(
            _.values(AnnotationValueType),
            function(valueType) {
              return new SpecimenLinkAnnotationType(
                jsonEntities.annotationType({ valueType: valueType }));
            });
          entities.annotationTypeIdsInUse = [entities.annotationTypes[0]];
        } else {
          entities.annotationTypes = [];
        }

        entities.specimenLinkTypes = _.map(_.range(2), function () {
          var slt = new SpecimenLinkType(jsonEntities.processingType(entities.study));
          if (options.studyHasSpecimenGroups) {
            slt.studySpecimenGroups(entities.specimenGroups);
          }
          return slt;
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
          '  <spc-link-types-panel',
          '     study="vm.study"',
          '     processing-dto="vm.processingDto">',
          '  </spc-link-types-panel>',
          '</uib-accordion>'
        ].join(''));

        scope = $rootScope.$new();
        scope.vm = {
          study: entities.study,
          processingDto: {
            processingTypes:             entities.processingTypes,
            specimenGroups:              entities.specimenGroups,
            specimenLinkAnnotationTypes: entities.annotationTypes,
            specimenLinkTypes:           entities.specimenLinkTypes
          }
        };

        $compile(element)(scope);
        scope.$digest();
        controller = element.find('spc-link-types-panel').controller('spcLinkTypesPanel');
      }
    }

    it('has valid scope', function () {
      var entities = createEntities();

      createController(entities);

      expect(controller.study).toBe(entities.study);
      _.each(entities.processingTypes, function (pt) {
        expect(controller.processingTypesById[pt.id]).toBe(pt);
      });
      _.each(entities.specimenGroups, function (sg) {
        expect(controller.specimenGroupsById[sg.id]).toBe(sg);
      });
      expect(controller.specimenLinkTypes).toBeArrayOfSize(entities.specimenLinkTypes.length);
      expect(controller.specimenLinkTypes).toContainAll(entities.specimenLinkTypes);
    });

    it('cannot add a specimen link type if study has no specimen groups', function() {
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

    it('can add specimen link type', function() {
      var state = this.$injector.get('$state'),
          entities = createEntities();

      createController(entities);
      spyOn(state, 'go').and.callFake(function () {});
      controller.add();
      scope.$digest();
      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.processing.spcLinkTypeAdd');
    });

    it('can view information for a processing type', function() {
      var EntityViewer = this.$injector.get('EntityViewer'),
          entities = createEntities();

      createController(entities);
      spyOn(EntityViewer.prototype, 'showModal').and.callFake(function () { });
      controller.viewProcessingType(entities.processingTypes[0].id);
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

    it('cannot update a specimen link type if study is not disabled', function() {
      var StudyStatus = this.$injector.get('StudyStatus'),
          entities    = createEntities(),
          statuses    = [StudyStatus.ENABLED, StudyStatus.RETIRED];

      createController(entities);
      _.each(statuses, function (status) {
        entities.study.status = status;

        expect(function () { controller.update(entities.specimenLinkTypes[0]); }).
          toThrow(new Error('study is not disabled'));
      });
    });

    it('can update a specimen link type', function() {
      var state = this.$injector.get('$state'),
          entities = createEntities();

      createController(entities);
      spyOn(state, 'go').and.callFake(function () {});

      controller.update(entities.specimenLinkTypes[0]);
      scope.$digest();

      expect(state.go).toHaveBeenCalledWith(
        'home.admin.studies.study.processing.spcLinkTypeUpdate', {
          procTypeId: entities.specimenLinkTypes[0].processingTypeId,
          spcLinkTypeId: entities.specimenLinkTypes[0].id });
    });

    it('cannot remove a specimen link type if study is not disabled', function() {
      var StudyStatus = this.$injector.get('StudyStatus'),
          entities    = createEntities(),
          statuses    = [StudyStatus.ENABLED, StudyStatus.RETIRED];

      scope = createController(entities);
      _.each(statuses, function (status) {
        entities.study.status = status;

        expect(function () { controller.remove(entities.specimenLinkTypes[0]); }).
          toThrow(new Error('study is not disabled'));
      });
    });

    it('can remove a specimen link type', function() {
      var q                   = this.$injector.get('$q'),
          domainEntityService = this.$injector.get('domainEntityService'),
          entities            = createEntities(),
          sltToRemove         = entities.specimenLinkTypes[0];

      createController(entities);
      spyOn(domainEntityService, 'removeEntity').and.callFake(function () {
        return q.when('OK');
      });
      controller.remove(sltToRemove);
      scope.$digest();
      expect(domainEntityService.removeEntity).toHaveBeenCalled();
      expect(controller.specimenLinkTypes).toBeArrayOfSize(entities.specimenLinkTypes.length - 1);
    });

    it('displays a modal if removal of a specimen link type fails', function() {
      var q                   = this.$injector.get('$q'),
          modalService        = this.$injector.get('modalService'),
          entities            = createEntities(),
          sltToRemove         = entities.specimenLinkTypes[0];

      createController(entities);
      spyOn(sltToRemove, 'remove').and.callFake(function () {
        var deferred = q.defer();
        deferred.reject('error');
        return deferred.promise;
      });
      spyOn(modalService, 'showModal').and.callFake(function () {
        return q.when('OK');
      });
      controller.remove(sltToRemove);
      scope.$digest();
      expect(modalService.showModal.calls.count()).toBe(2);
    });

  });

});

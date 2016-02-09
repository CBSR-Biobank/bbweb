/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
// Jasmine test suite
//
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, testUtils) {
  'use strict';

  describe('Directive: studyAnnotationTypesPanelDirective', function() {
    var Study,
        ParticipantAnnotationType,
        CollectionEventAnnotationType,
        SpecimenLinkAnnotationType,
        AnnotationValueType,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function () {
      Study                         = this.$injector.get('Study');
      ParticipantAnnotationType     = this.$injector.get('ParticipantAnnotationType');
      CollectionEventAnnotationType = this.$injector.get('CollectionEventAnnotationType');
      SpecimenLinkAnnotationType    = this.$injector.get('SpecimenLinkAnnotationType');
      AnnotationValueType           = this.$injector.get('AnnotationValueType');
      fakeEntities                  = this.$injector.get('fakeDomainEntities');
    }));

    describe('for Participant Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        context.study = new Study(fakeEntities.study());
        context.annotationTypes = _.map(
          AnnotationValueType.values(),
          function(valueType) {
          return new ParticipantAnnotationType(
            fakeEntities.studyAnnotationType(
              context.study, {valueType: valueType, required: true}));
        });
        context.annotationTypeIdsInUse = [context.annotationTypes[0]];
        context.annotationTypeName     = 'ParticipantAnnotationType';
        context.panelId                = 'study.panel.participantAnnotationTypes';
        context.addStateName           = 'home.admin.studies.study.participants.annotationTypeAdd';
      });

      sharedBehaviour(context);
    });

    describe('for Collection Event Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        context.study = new Study(fakeEntities.study());
        context.annotationTypes = _.map(
          AnnotationValueType.values(),
          function(valueType) {
          return new CollectionEventAnnotationType(
            fakeEntities.studyAnnotationType(
              context.study, {valueType: valueType, required: true}));
        });
        context.annotationTypeIdsInUse = [context.annotationTypes[0]];
        context.annotationTypeName   = 'CollectionEventAnnotationType';
        context.panelId         = 'study.panel.collectionEventAnnotationTypes';
        context.addStateName    = 'home.admin.studies.study.collection.ceventAnnotationTypeAdd';
      });

      sharedBehaviour(context);
    });

    describe('for Specimen Link Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        context.study = new Study(fakeEntities.study());
        context.annotationTypes = _.map(
          AnnotationValueType.values(),
          function(valueType) {
          return new SpecimenLinkAnnotationType(
            fakeEntities.studyAnnotationType(
              context.study, {valueType: valueType, required: true}));
        });
        context.annotationTypeIdsInUse = [context.annotationTypes[0]];
        context.annotationTypeName   = 'SpecimenLinkAnnotationType';
        context.panelId         = 'study.panel.specimenLinkAnnotationTypes';
        context.addStateName    = 'home.admin.studies.study.processing.spcLinkAnnotationTypeAdd';
      });

      sharedBehaviour(context);
    });

    function sharedBehaviour(context) {

      describe('(shared)', function() {
        var scope,
            controller,
            $state,
            Panel;

        beforeEach(inject(function($window,
                                   $compile,
                                   $rootScope) {
          var element,
              $templateCache = this.$injector.get('$templateCache');

          Panel = this.$injector.get('Panel');
          $state = this.$injector.get('$state');

          spyOn($state, 'go').and.callFake(function () {});
          spyOn(Panel.prototype, 'add').and.callThrough();

          $window.localStorage.setItem(context.panelId, '');

          testUtils.putHtmlTemplates(
            $templateCache,
            '/assets/javascripts/admin/studies/annotationTypes/directives/studyAnnotationTypesPanel/studyAnnotationTypesPanel.html',
            '/assets/javascripts/admin/studies/annotationTypes/directives/studyAnnotationTypesTable/studyAnnotationTypesTable.html',
            '/assets/javascripts/common/directives/panelButtons.html',
            '/assets/javascripts/common/directives/updateRemoveButtons.html');


          element = angular.element([
            '<uib-accordion close-others="false">',
            '  <study-annotation-types-panel',
            '     study="vm.study"',
            '     annotation-types="vm.annotationTypes"',
            '     annotation-type-ids-in-use="vm.annotationTypeIdsInUse"',
            '     annotation-type-name="' + context.annotationTypeName + '"',
            '     panel-id="' + context.panelId + '"',
            '     add-state-name="' + context.addStateName + '"',
            '     update-state-name="' + context.updateStateName + '">',
            '  </study-annotation-types-panel>',
            '</uib-accordion>'
          ].join(''));

          scope = $rootScope.$new();
          scope.vm = {
            study:                  context.study,
            annotationTypes:        context.annotationTypes,
            annotationTypeIdsInUse: context.annotationTypeIdsInUse
          };

          $compile(element)(scope);
          scope.$digest();
          controller = element.find('study-annotation-types-panel')
            .controller('studyAnnotationTypesPanel');
        }));

        it('has valid scope', function () {
          expect(controller.study).toEqual(context.study);
          expect(controller.annotationTypes).toEqual(context.annotationTypes);
          expect(controller.annotationTypeIdsInUse).toEqual(context.annotationTypeIdsInUse);
        });

        it('has valid description', function () {
          expect(controller.annotationTypeDescription)
            .toContain(getDescriptionSubString(context.annotationTypeName));

          function getDescriptionSubString(annotationTypeName) {
            switch (annotationTypeName) {
            case 'ParticipantAnnotationType':
              return 'Participant';

            case 'CollectionEventAnnotationType':
              return 'Collection event';

            case 'SpecimenLinkAnnotationType':
              return 'Specimen link';

            default:
              jasmine.getEnv().fail('annotationTypeName is invalid: ' + annotationTypeName);
              return '';
            }
          }
        });

        it('should invoke panel add function', function() {
          controller.add();
          expect(Panel.prototype.add).toHaveBeenCalled();
          expect($state.go).toHaveBeenCalledWith(context.addStateName);
        });

      });

    }
  });

});

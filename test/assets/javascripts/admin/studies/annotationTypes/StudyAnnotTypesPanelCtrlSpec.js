// Jasmine test suite
//
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Controller: StudyAnnotTypesPanelCtrl', function() {
    var Study,
        ParticipantAnnotationType,
        CollectionEventAnnotationType,
        SpecimenLinkAnnotationType,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (_Study_,
                                _ParticipantAnnotationType_,
                                _CollectionEventAnnotationType_,
                                _SpecimenLinkAnnotationType_,
                                fakeDomainEntities) {
      Study                         = _Study_;
      ParticipantAnnotationType     = _ParticipantAnnotationType_;
      CollectionEventAnnotationType = _CollectionEventAnnotationType_;
      SpecimenLinkAnnotationType    = _SpecimenLinkAnnotationType_;
      fakeEntities                  = fakeDomainEntities;
    }));

    describe('for Participant Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        context.study = new Study(fakeEntities.study());
        context.annotationTypes = _.map(['Text', 'Number', 'DateTime', 'Select'], function(valueType) {
          return new ParticipantAnnotationType(
            fakeEntities.studyAnnotationType(
              context.study, {valueType: valueType, required: true}));
        });
        context.annotationTypeIdsInUse = [context.annotationTypes[0]];
        context.annotationTypeName   = 'ParticipantAnnotationType';
        context.panelId         = 'study.panel.participantAnnotationTypes';
        context.addStateName    = 'home.admin.studies.study.participants.annotationTypeAdd';
      });

      sharedBehaviour(context);
    });

    describe('for Collection Event Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        context.study = new Study(fakeEntities.study());
        context.annotationTypes = _.map(['Text', 'Number', 'DateTime', 'Select'], function(valueType) {
          return new CollectionEventAnnotationType(
            fakeEntities.studyAnnotationType(
              context.study, {valueType: valueType, required: true}));
        });
        context.annotationTypeIdsInUse = [context.annotationTypes[0]];
        context.annotationTypeName   = 'CollectionEventAnnotationType';
        context.panelId         = 'study.panel.collectionEventAnnotationTypes';
        context.addStateName    = 'home.admin.studies.study.collection.ceventAnnotTypeAdd';
      });

      sharedBehaviour(context);
    });

    describe('for Specimen Link Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        context.study = new Study(fakeEntities.study());
        context.annotationTypes = _.map(['Text', 'Number', 'DateTime', 'Select'], function(valueType) {
          return new SpecimenLinkAnnotationType(
            fakeEntities.studyAnnotationType(
              context.study, {valueType: valueType, required: true}));
        });
        context.annotationTypeIdsInUse = [context.annotationTypes[0]];
        context.annotationTypeName   = 'SpecimenLinkAnnotationType';
        context.panelId         = 'study.panel.specimenLinkAnnotationTypes';
        context.addStateName    = 'home.admin.studies.study.processing.spcLinkAnnotTypeAdd';
      });

      sharedBehaviour(context);
    });

    function sharedBehaviour(context) {

      var panelId;

      describe('(shared)', function() {
        var scope,
            state,
            Panel,
            study,
            annotationTypes,
            annotationTypeIdsInUse,
            annotationTypeName,
            addStateName;

        beforeEach(inject(function($window,
                                   $state,
                                   $controller,
                                   $rootScope,
                                   _Panel_) {

          Panel             = _Panel_;
          state             = $state;
          study             = context.study;
          annotationTypes        = context.annotationTypes;
          annotationTypeIdsInUse = context.annotationTypeIdsInUse;
          annotationTypeName     = context.annotationTypeName;
          panelId           = context.panelId;
          addStateName      = context.addStateName;

          spyOn(state, 'go').and.callFake(function () {});
          spyOn(Panel.prototype, 'add').and.callThrough();

          $window.localStorage.setItem(panelId, '');

          scope = $rootScope.$new();
          scope.study           = study;
          scope.annotationTypes      = annotationTypes;
          scope.annotationTypeIdsInUse = annotationTypeIdsInUse;
          scope.annotationTypeName   = context.annotationTypeName;
          scope.addStateName    = addStateName;

          $controller('StudyAnnotTypesPanelCtrl as vm', {
            $scope: scope,
            Panel:  Panel
          });
          scope.$digest();
        }));

        it('has valid scope', function () {
          expect(scope.vm.study).toEqual(study);
          expect(scope.vm.annotationTypes).toEqual(annotationTypes);
          expect(scope.vm.annotationTypeIdsInUse).toEqual(annotationTypeIdsInUse);
        });

        it('has valid panel heading', function () {

          expect(scope.vm.panelHeading).toEqual(getHeading(annotationTypeName));

          function getHeading(annotationTypeName) {
            switch (annotationTypeName) {
            case 'ParticipantAnnotationType':
              return 'Participant Annotation Types';

            case 'CollectionEventAnnotationType':
              return 'Collection Event Annotation Types';

            case 'SpecimenLinkAnnotationType':
              return 'Specimen Link Annotation Types';

            default:
              jasmine.getEnv().fail('annotationTypeName is invalid: ' + annotationTypeName);
              return '';
            }
          }
        });

        it('has valid description', function () {
          expect(scope.vm.annotationTypeDescription).toContain(getDescriptionSubString(annotationTypeName));

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
          scope.vm.add();
          expect(Panel.prototype.add).toHaveBeenCalled();
          expect(state.go).toHaveBeenCalledWith(addStateName);
        });

      });

    }
  });

});

/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 *
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('Directive: studyAnnotationTypesTableDirective', function() {

    var Study,
        ParticipantAnnotationType,
        CollectionEventAnnotationType,
        SpecimenLinkAnnotationType,
        AnnotationValueType,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (_Study_,
                                _ParticipantAnnotationType_,
                                _CollectionEventAnnotationType_,
                                _SpecimenLinkAnnotationType_,
                                _AnnotationValueType_,
                                fakeDomainEntities) {
      Study                         = _Study_;
      ParticipantAnnotationType     = _ParticipantAnnotationType_;
      CollectionEventAnnotationType = _CollectionEventAnnotationType_;
      SpecimenLinkAnnotationType    = _SpecimenLinkAnnotationType_;
      AnnotationValueType           = _AnnotationValueType_;
      fakeEntities                  = fakeDomainEntities;
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
                context.study,
                { valueType: valueType, required: true }));
          });
        context.annotationTypeIdsInUse = [context.annotationTypes[0].id];
        context.annotationTypeName   = 'ParticipantAnnotationType';
        context.updateStateName = 'home.admin.studies.study.participants.annotationTypeUpdate';
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
                context.study,
                { valueType: valueType }));
          });
        context.annotationTypeIdsInUse = [context.annotationTypes[0].id];
        context.annotationTypeName   = 'CollectionEventAnnotationType';
        context.updateStateName = 'home.admin.studies.study.collection.ceventAnnotationTypeUpdate';
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
                context.study,
                { valueType: valueType }));
          });
        context.annotationTypeIdsInUse = [context.annotationTypes[0].id];
        context.annotationTypeName   = 'SpecimenLinkAnnotationType';
        context.updateStateName = 'home.admin.studies.study.processing.spcLinkAnnotationTypeUpdate';
      });

      sharedBehaviour(context);
    });

    function sharedBehaviour(context) {

      var controller,
          scope,
          state,
          modalService,
          studyAnnotationTypeUtils,
          AnnotationTypeViewer;

      describe('(shared)', function() {

        beforeEach(inject(function($rootScope,
                                   $compile,
                                   testUtils) {
          var element,
              hasRequired = (context.annotationTypeName === 'ParticipantAnnotationType');

          state                    = this.$injector.get('$state');
          modalService             = this.$injector.get('modalService');
          studyAnnotationTypeUtils = this.$injector.get('studyAnnotationTypeUtils');
          AnnotationTypeViewer     = this.$injector.get('AnnotationTypeViewer');

          testUtils.putHtmlTemplates(
            '/assets/javascripts/admin/studies/annotationTypes/directives/studyAnnotationTypesTable/studyAnnotationTypesTable.html',
            '/assets/javascripts/common/directives/updateRemoveButtons.html');

          spyOn(state, 'go');

          element = angular.element([
            '<study-annotation-types-table',
            '   study="vm.study"',
            '   annotation-types="vm.annotationTypes"',
            '   annotation-type-name="vm.annotationTypeName"',
            '   annotation-type-ids-in-use="vm.annotationTypeIdsInUse"',
            '   update-state-name="vm.updateStateName"',
            '   has-required="' + hasRequired + '">',
            '</study-annotation-types-table>'
          ].join(''));

          scope = $rootScope.$new();
          scope.vm = {
            study:                  context.study,
            annotationTypes:        context.annotationTypes,
            annotationTypeIdsInUse: context.annotationTypeIdsInUse,
            annotationTypeName:     context.annotationTypeName,
            updateStateName:        context.updateStateName
          };

          $compile(element)(scope);
          scope.$digest();
          controller = element.controller('studyAnnotationTypesTable');
        }));

        it('has valid scope', function() {
          expect(controller.annotationTypes).toEqual(context.annotationTypes);
          expect(controller.annotationTypeIdsInUse).toEqual(context.annotationTypeIdsInUse);
          expect(controller.tableParams).not.toBeNull();
          expect(controller.modificationsAllowed).toEqual(context.study.isDisabled());
        });

        it('has valid table columns', function() {
          if (context.annotationTypeName === 'ParticipantAnnotationType') {
            expect(controller.columns).toBeArrayOfSize(4);
            expect(controller.columns[2].title).toBe('Required');
            expect(controller.columns[3].title).toBe('Description');
          } else {
            expect(controller.columns).toBeArrayOfSize(3);
            expect(controller.columns[2].title).toBe('Description');
          }

          expect(controller.columns[0].title).toBe('Name');
          expect(controller.columns[1].title).toBe('Type');
        });

        it('calling information should open an AnnotationTypeViewer', function() {
          expect(controller.information(controller.annotationTypes[0]))
            .toEqual(jasmine.any(AnnotationTypeViewer));
        });

        it('on update should open a modal to display annotation type in use modal', function() {
          spyOn(modalService, 'modalOk');
          controller.update(controller.annotationTypes[0]);
          expect(modalService.modalOk).toHaveBeenCalled();
        });

        it('on update should change state to update an annotation type', function() {
          var annotationTypeToUpdate = controller.annotationTypes[1];
          spyOn(modalService, 'modalOk');
          controller.update(annotationTypeToUpdate);
          expect(state.go).toHaveBeenCalledWith(context.updateStateName,
                                                { annotationTypeId: annotationTypeToUpdate.id });
        });

        it('on remove opens a modal to display annotation type in use modal', function() {
          spyOn(modalService, 'modalOk');
          controller.remove(controller.annotationTypes[0]);
          expect(modalService.modalOk).toHaveBeenCalled();
        });

        it('can remove an annotation type', function() {
          var $q = this.$injector.get('$q');
          spyOn(studyAnnotationTypeUtils, 'remove').and.callFake(function () {
            var deferred = $q.defer();
            deferred.resolve('xxx');
            return deferred.promise;
          });
          controller.remove(controller.annotationTypes[1]);
          scope.$digest();
          expect(_.pluck(controller.annotationTypes, 'id')).not.toContain(context.annotationTypes[1].id);
        });

        it('on remove failure the annotation type is not removed and fail dialog is displayed', function() {
          var $q = this.$injector.get('$q'),
              annotationTypeToRemove = context.annotationTypes[2];

          spyOn(modalService, 'showModal').and.callFake(function () {
            var deferred = $q.defer();
            deferred.resolve('xxx');
            return deferred.promise;

          });

          spyOn(annotationTypeToRemove, 'remove').and.callFake(function () {
            var deferred = $q.defer();
            deferred.reject('xxx');
            return deferred.promise;
          });

          controller.remove(annotationTypeToRemove);
          scope.$digest();
          expect(annotationTypeToRemove.remove).toHaveBeenCalled();
          expect(modalService.showModal.calls.count()).toEqual(2);
          expect(_.pluck(controller.annotationTypes, 'id')).toContain(annotationTypeToRemove.id);
        });

        it('update fails for a disabled study', function() {
          var StudySatus = this.$injector.get('StudyStatus');
          context.study.status = StudySatus.ENABLED();
          expect(function () {
            controller.update(controller.annotationTypes[1]);
          }).toThrow(new Error('study is not disabled'));
        });

        it('remove fails for a disabled study', function() {
          var StudySatus = this.$injector.get('StudyStatus');
          context.study.status = StudySatus.ENABLED();
          expect(function () {
            controller.remove(controller.annotationTypes[1]);
          }).toThrow(new Error('study is not disabled'));

        });

      });
    }

  });

});

// Jasmine test suite
//
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _, commonTests) {
  'use strict';

  describe('Controller: StudyAnnotTypesTableCtrl', function() {

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
        context.annotTypes = _.map(
          ['Text', 'Number', 'DateTime', 'Select'],
          function(valueType) {
            return new ParticipantAnnotationType(
              fakeEntities.studyAnnotationType(
                context.study,
                { valueType: valueType, required: true }));
          });
        context.annotTypeIdsInUse = [context.annotTypes[0].id];
        context.annotTypeName   = 'ParticipantAnnotationType';
        context.updateStateName = 'home.admin.studies.study.participants.annotTypeUpdate';
        });

      sharedBehaviour(context);
    });

    describe('for Collection Event Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        context.study = new Study(fakeEntities.study());
        context.annotTypes = _.map(
          ['Text', 'Number', 'DateTime', 'Select'],
          function(valueType) {
            return new CollectionEventAnnotationType(
              fakeEntities.studyAnnotationType(
                context.study,
                { valueType: valueType }));
          });
        context.annotTypeIdsInUse = [context.annotTypes[0].id];
        context.annotTypeName   = 'CollectionEventAnnotationType';
        context.updateStateName = 'home.admin.studies.study.collection.ceventAnnotTypeUpdate';
        });

      sharedBehaviour(context);
    });

    describe('for Specimen Link Annotation Types', function() {
      var context = {};

      beforeEach(function () {
        context.study = new Study(fakeEntities.study());
        context.annotTypes = _.map(
          ['Text', 'Number', 'DateTime', 'Select'],
          function(valueType) {
            return new SpecimenLinkAnnotationType(
              fakeEntities.studyAnnotationType(
                context.study,
                { valueType: valueType }));
          });
        context.annotTypeIdsInUse = [context.annotTypes[0].id];
        context.annotTypeName   = 'SpecimenLinkAnnotationType';
        context.updateStateName = 'home.admin.studies.study.processing.spcLinkAnnotTypeUpdate';
        });

      sharedBehaviour(context);
    });

    function sharedBehaviour(context) {

      var scope,
          state,
          modalService,
          tableService,
          studyAnnotationTypeUtils,
          AnnotationTypeViewer;

      describe('(shared)', function() {

        beforeEach(inject(function($state,
                                   $rootScope,
                                   $controller,
                                   _modalService_,
                                   _studyAnnotationTypeUtils_,
                                   _AnnotationTypeViewer_,
                                   _tableService_) {
          state                    = $state;
          modalService             = _modalService_;
          studyAnnotationTypeUtils = _studyAnnotationTypeUtils_;
          AnnotationTypeViewer     = _AnnotationTypeViewer_;
          tableService             = _tableService_;

          scope                    = $rootScope.$new();
          scope.study              = context.study;
          scope.annotTypes         = context.annotTypes;
          scope.annotTypeIdsInUse  = context.annotTypeIdsInUse;
          scope.annotTypeName      = context.annotTypeName;
          scope.updateStateName    = context.updateStateName;
          scope.hasRequired        = (scope.annotTypeName === 'ParticipantAnnotationType');

          spyOn(state, 'go');
          spyOn(tableService, 'getTableParams').and.callThrough();

          $controller('StudyAnnotTypesTableCtrl as vm', {
            $scope:                   scope,
            $state:                   state,
            modalService:             modalService,
            studyAnnotationTypeUtils: studyAnnotationTypeUtils,
            AnnotationTypeViewer:     AnnotationTypeViewer
          });
          scope.$digest();

          spyOn(scope.vm.tableParams, 'reload').and.callFake(function () {});
        }));

        it('has valid scope', function() {
          expect(scope.vm.annotTypes).toEqual(scope.annotTypes);
          expect(scope.vm.annotTypeIdsInUse).toEqual(scope.annotTypeIdsInUse);
          expect(scope.vm.tableParams).not.toBeNull();
          expect(scope.vm.modificationsAllowed).toEqual(scope.study.isDisabled());
        });

        it('scope copies annotation types', function() {
          _.each(_.zip(scope.vm.annotTypes, scope.annotTypes), function(tuple) {
            expect(tuple[0].id).toBe(tuple[1].id);
            expect(tuple[0]).not.toBe(tuple[1]);
          });
        });

        it('has valid table columns', function() {
          if (scope.annotTypeName === 'ParticipantAnnotationType') {
            expect(scope.vm.columns).toBeArrayOfSize(4);
            expect(scope.vm.columns[2].title).toBe('Required');
            expect(scope.vm.columns[3].title).toBe('Description');
          } else {
            expect(scope.vm.columns).toBeArrayOfSize(3);
            expect(scope.vm.columns[2].title).toBe('Description');
          }

          expect(scope.vm.columns[0].title).toBe('Name');
          expect(scope.vm.columns[1].title).toBe('Type');
        });

        it('scope copies annotation types', function() {
          _.each(_.zip(scope.vm.annotTypes, scope.annotTypes), function(tuple) {
            expect(tuple[0].id).toBe(tuple[1].id);
            expect(tuple[0]).not.toBe(tuple[1]);
          });
        });

        it('calling information should open an AnnotationTypeViewer', function() {
          expect(scope.vm.information(scope.vm.annotTypes[0]))
            .toEqual(jasmine.any(AnnotationTypeViewer));
        });

        it('on update should open a modal to display annotation type in use modal', function() {
          spyOn(studyAnnotationTypeUtils, 'inUseModal');
          scope.vm.update(scope.vm.annotTypes[0]);
          expect(studyAnnotationTypeUtils.inUseModal).toHaveBeenCalled();
        });

        it('on update should change state to update an annotation type', function() {
          var annotTypeToUpdate = scope.vm.annotTypes[1];
          spyOn(modalService, 'modalOk');
          scope.vm.update(annotTypeToUpdate);
          expect(state.go).toHaveBeenCalledWith(scope.updateStateName,
                                                { annotTypeId: annotTypeToUpdate.id });
        });

        it('on remove opens a modal to display annotation type in use modal', function() {
          spyOn(modalService, 'modalOk');
          scope.vm.remove(scope.vm.annotTypes[0]);
          expect(modalService.modalOk).toHaveBeenCalled();
        });

        it('can remove an annotation type', function() {
          var $q = this.$injector.get('$q');
          spyOn(studyAnnotationTypeUtils, 'remove').and.callFake(function () {
            var deferred = $q.defer();
            deferred.resolve('xxx');
            return deferred.promise;
          });
          scope.vm.remove(scope.vm.annotTypes[1]);
          scope.$digest();
          expect(_.pluck(scope.vm.annotTypes, 'id')).not.toContain(scope.annotTypes[1].id);
        });

        it('on remove failure the annotation type is not removed and fail dialog is displayed', function() {
          var $q = this.$injector.get('$q'),
              annotationTypeToRemove = scope.annotTypes[2];

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

          scope.vm.remove(annotationTypeToRemove);
          scope.$digest();
          expect(annotationTypeToRemove.remove).toHaveBeenCalled();
          expect(modalService.showModal.calls.count()).toEqual(2);
          expect(_.pluck(scope.vm.annotTypes, 'id')).toContain(annotationTypeToRemove.id);
        });

        it('update fails for a disabled study', function() {
          var StudySatus = this.$injector.get('StudyStatus');
          scope.study.status = StudySatus.ENABLED();
          expect(function () {
            scope.vm.update(scope.vm.annotTypes[1]);
          }).toThrow(new Error('study is not disabled'));
        });

        it('remove fails for a disabled study', function() {
          var StudySatus = this.$injector.get('StudyStatus');
          scope.study.status = StudySatus.ENABLED();
          expect(function () {
            scope.vm.remove(scope.vm.annotTypes[1]);
          }).toThrow(new Error('study is not disabled'));

        });

      });
    }

  });

});

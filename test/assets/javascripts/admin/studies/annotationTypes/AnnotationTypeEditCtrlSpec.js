// Jasmine test suite
//
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('Controller: AnnotationTypeEditCtrl', function() {
    var scope,
        state,
        controller,
        rootScope,
        fakeEntities,
        domainEntityService,
        notificationsService,
        ParticipantAnnotationType,
        AnnotationValueType,
        study;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($state,
                               $controller,
                               $rootScope,
                               _domainEntityService_,
                               _notificationsService_,
                               Study,
                               _ParticipantAnnotationType_,
                               _AnnotationValueType_,
                               fakeDomainEntities) {
      state                     = $state;
      controller                = $controller;
      rootScope                 = $rootScope;
      fakeEntities              = fakeDomainEntities;
      domainEntityService   = _domainEntityService_;
      notificationsService      = _notificationsService_;
      ParticipantAnnotationType = _ParticipantAnnotationType_;
      AnnotationValueType       = _AnnotationValueType_;

      study = new Study(fakeEntities.study());

      spyOn(state, 'go');
    }));

    describe('for participant annotation types', function() {
      var context = {};

      beforeEach(inject(function (ParticipantAnnotationType) {
        var baseAnnotType = fakeEntities.annotationType({ required: true }),
            stateName = 'home.admin.studies.study.participants';


        context.state           = { current: { name: stateName } };
        context.returnState     = stateName;
        context.annotationTypeNew    = new ParticipantAnnotationType(_.omit(baseAnnotType, 'id'));
        context.annotationTypeWithId = new ParticipantAnnotationType(baseAnnotType);
      }));

      sharedBehaviour(context);
    });

    describe('for collection event annotation types', function() {
      var context = {};

      beforeEach(inject(function (CollectionEventAnnotationType) {
        var baseAnnotType = fakeEntities.annotationType();

        context.state           = { current: { name: 'home.admin.studies.study.collection' } };
        context.returnState     = 'home.admin.studies.study.collection';
        context.annotationTypeNew    = new CollectionEventAnnotationType(_.omit(baseAnnotType, 'id'));
        context.annotationTypeWithId = new CollectionEventAnnotationType(baseAnnotType);
      }));

      sharedBehaviour(context);
    });

    describe('for specimen link annotation types', function() {
      var context = {};

      beforeEach(inject(function (SpecimenLinkAnnotationType) {
        var baseAnnotType = fakeEntities.annotationType({ required: true }),
            stateName = 'home.admin.studies.study.processing';


        context.state           = { current: { name: stateName } };
        context.returnState     = stateName;
        context.annotationTypeNew    = new SpecimenLinkAnnotationType(_.omit(baseAnnotType, 'id'));
        context.annotationTypeWithId = new SpecimenLinkAnnotationType(baseAnnotType);
      }));

      sharedBehaviour(context);
    });


    function sharedBehaviour(context) {

      describe('(shared)', function() {

        var q, state, returnState, annotationTypeNew, annotationTypeWithId, ParticipantAnnotationType;

        beforeEach(inject(function ($q, _ParticipantAnnotationType_) {
          q               = $q;
          state           = context.state;
          returnState     = context.returnState;
          annotationTypeNew    = context.annotationTypeNew;
          annotationTypeWithId = context.annotationTypeWithId;
          ParticipantAnnotationType = _ParticipantAnnotationType_;

          state.go = function () {};
          spyOn(state, 'go');
        }));

        it('scope should be valid when adding', function() {
          createController(state, annotationTypeNew);

          expect(scope.vm.study).toEqual(study);
          expect(scope.vm.annotationType).toEqual(annotationTypeNew);
          expect(scope.vm.title).toBe('Add Annotation Type');
          expect(scope.vm.hasRequiredField).toEqual(annotationTypeNew instanceof ParticipantAnnotationType);
          expect(scope.vm.valueTypes).toEqual(AnnotationValueType.values());
        });

        it('scope should be valid when updating', function() {
          createController(state, annotationTypeWithId);

          expect(scope.vm.study).toEqual(study);
          expect(scope.vm.annotationType).toEqual(annotationTypeWithId);
          expect(scope.vm.title).toBe('Update Annotation Type');
          expect(scope.vm.hasRequiredField).toEqual(annotationTypeWithId instanceof ParticipantAnnotationType);
          expect(scope.vm.valueTypes).toEqual(AnnotationValueType.values());
        });

        it('throws an exception if the current state is invalid', function() {
          var invalidState = { current: { name: 'xyz' } };
          expect(function () {
            createController(invalidState, annotationTypeWithId);
          }).toThrow(new Error('invalid current state name: ' + invalidState.current.name));
        });

        it('maxValueCountRequired is valid', function() {
          annotationTypeWithId.valueType = AnnotationValueType.SELECT();
          annotationTypeWithId.maxValueCount = 0;
          createController(state, annotationTypeWithId);

          annotationTypeWithId.maxValueCount = 0;
          expect(scope.vm.maxValueCountRequired()).toBe(true);

          annotationTypeWithId.maxValueCount = 3;
          expect(scope.vm.maxValueCountRequired()).toBe(true);

          annotationTypeWithId.maxValueCount = 1;
          expect(scope.vm.maxValueCountRequired()).toBe(false);

          annotationTypeWithId.maxValueCount = 2;
          expect(scope.vm.maxValueCountRequired()).toBe(false);
        });

        it('calling valueTypeChange clears the options array', function() {
          annotationTypeWithId.valueType = 'Select';
          annotationTypeWithId.maxValueCount = 1;
          createController(state, annotationTypeWithId);

          scope.vm.valueTypeChange();
          expect(annotationTypeWithId.options).toBeArray();
          expect(annotationTypeWithId.options).toBeEmptyArray();
        });

        it('calling optionAdd creates the options array', function() {
          createController(state, annotationTypeWithId);
          annotationTypeWithId.valueType = 'Select';
          annotationTypeWithId.maxValueCount = 1;
          annotationTypeWithId.valueTypeChanged();

          scope.vm.optionAdd();
          expect(annotationTypeWithId.options).toBeArrayOfSize(1);
          expect(annotationTypeWithId.options).toBeArrayOfStrings();
        });

        it('calling optionAdd appends to the options array', function() {
          annotationTypeWithId.valueType = 'Select';
          annotationTypeWithId.maxValueCount = 1;
          annotationTypeWithId.valueTypeChanged();
          createController(state, annotationTypeWithId);

          scope.vm.optionAdd();
          expect(annotationTypeWithId.options).toBeArrayOfSize(1);
          expect(annotationTypeWithId.options).toBeArrayOfStrings();
        });

        it('calling optionRemove throws an error on empty array', function() {
          annotationTypeWithId.valueType = 'Select';
          annotationTypeWithId.maxValueCount = 1;
          annotationTypeWithId.valueTypeChanged();
          createController(state, annotationTypeWithId);
          expect(function () { scope.vm.optionRemove('abc'); }).toThrow();
        });

        it('calling optionRemove throws an error if removal results in empty array', function() {
          annotationTypeWithId.valueType = 'Select';
          annotationTypeWithId.maxValueCount = 1;
          annotationTypeWithId.valueTypeChanged();
          annotationTypeWithId.options = ['abc'];
          createController(state, annotationTypeWithId);
          expect(function () { scope.vm.optionRemove('abc'); }).toThrow();
        });

        it('calling optionRemove removes an option', function() {
          // note: more than two strings in options array
          var options = ['abc', 'def'];
          annotationTypeWithId.valueType = 'Select';
          annotationTypeWithId.maxValueCount = 1;
          annotationTypeWithId.valueTypeChanged();
          annotationTypeWithId.options = options.slice(0);
          createController(state, annotationTypeWithId);
          scope.vm.optionRemove('abc');
          expect(annotationTypeWithId.options).toBeArrayOfSize(options.length - 1);
        });

        it('calling removeButtonDisabled returns valid results', function() {
          // note: more than two strings in options array
          var options = ['abc', 'def'];
          annotationTypeWithId.valueType = 'Select';
          annotationTypeWithId.maxValueCount = 1;
          annotationTypeWithId.valueTypeChanged();
          annotationTypeWithId.options = options.slice(0);
          createController(state, annotationTypeWithId);

          expect(scope.vm.removeButtonDisabled()).toEqual(false);

          annotationTypeWithId.options = options.slice(1);
          expect(scope.vm.removeButtonDisabled()).toEqual(true);
        });

        it('when adding should return to the valid state on submit', function() {
          onSubmit(state, annotationTypeNew, returnState);
        });

        it('when adding should return to the valid state on cancel', function() {
          onCancel(state, annotationTypeNew, returnState);
        });

        it('when submitting and server responds with an error, the error message is displayed', function() {
          var domainEntityService = this.$injector.get('domainEntityService');

          spyOn(domainEntityService, 'updateErrorModal')
            .and.callFake(function () {});

          spyOnAnnotTypeAddOrUpdateAndReject(annotationTypeNew);

          createController(state, annotationTypeNew);
          scope.vm.submit(annotationTypeNew);
          scope.$digest();

          expect(domainEntityService.updateErrorModal).toHaveBeenCalled();
        });

        it('when updating should return to the valid state on submit', function() {
          onSubmit(state, annotationTypeWithId, returnState);
        });

        it('when updating should return to the valid state on cancel', function() {
          onCancel(state, annotationTypeWithId, returnState);
        });

        function createController(state, annotationType) {
          scope = rootScope.$new();

          controller('AnnotationTypeEditCtrl as vm', {
            $scope:                    scope,
            $state:                    state,
            notificationsService:      notificationsService,
            domainEntityService:   domainEntityService,
            ParticipantAnnotationType: ParticipantAnnotationType,
            AnnotationValueType:       AnnotationValueType,
            study:                     study,
            annotationType:                 annotationType
          });
          scope.$digest();
        }

        function spyOnAnnotTypeAddOrUpdateAndResolve(annotationType) {
          spyOn(annotationType, 'addOrUpdate').and.callFake(function () {
            var deferred = q.defer();
            deferred.resolve('xxx');
            return deferred.promise;
          });
        }

        function spyOnAnnotTypeAddOrUpdateAndReject(annotationType) {
          spyOn(annotationType, 'addOrUpdate').and.callFake(function () {
            var deferred = q.defer();
            deferred.reject({ data: { message: 'error'} });
            return deferred.promise;
          });
        }

        function onSubmit(state, annotationType, returnState) {
          spyOnAnnotTypeAddOrUpdateAndResolve(annotationType);

          createController(state, annotationType);
          scope.vm.submit(annotationType);
          scope.$digest();
          expect(state.go).toHaveBeenCalledWith(
            returnState, {studyId: study.id}, {reload: true});
        }

        function onCancel(state, annotationType, returnState) {
          spyOnAnnotTypeAddOrUpdateAndResolve(annotationType);

          createController(state, annotationType);
          scope.vm.cancel();
          scope.$digest();
          expect(state.go).toHaveBeenCalledWith(
            returnState, {studyId: study.id}, {reload: true});
        }

      });
    }

  });

});

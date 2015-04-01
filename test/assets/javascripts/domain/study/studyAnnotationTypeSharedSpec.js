/**
 * Jasmine test suite
 */
define(['underscore'], function(_) {
  'use strict';

  /**
   * Shared spec for all 3 services listed above.
   */
  function studyAnnotationTypeSharedSpec(context) {

    describe('(shared)', function() {

      var httpBackend,
          funutils,
          AnnotTypeType,
          AnnotationValueType,
          AnnotationMaxValueCount,
          createAnnotTypeFn,
          annotationTypesService,
          annotationTypeUriPart,
          objRequiredKeys,
          createServerAnnotTypeFn,
          annotationTypeListFn,
          annotationTypeGetFn;

      beforeEach(inject(function($httpBackend,
                                 _funutils_,
                                 _AnnotationValueType_,
                                 _AnnotationMaxValueCount_) {
        httpBackend              = $httpBackend;
        funutils                 = _funutils_;
        AnnotationValueType      = _AnnotationValueType_;
        AnnotationMaxValueCount  = _AnnotationMaxValueCount_;
        AnnotTypeType            = context.annotationTypeType;
        createAnnotTypeFn        = context.createAnnotTypeFn;
        annotationTypesService        = context.annotationTypesService;
        annotationTypeUriPart         = context.annotationTypeUriPart;
        objRequiredKeys          = context.objRequiredKeys;
        createServerAnnotTypeFn  = context.createServerAnnotTypeFn;
        annotationTypeListFn          = context.annotationTypeListFn;
        annotationTypeGetFn           = context.annotationTypeGetFn;
      }));

      it('has default values', function() {
        var annotationType = new AnnotTypeType();

        expect(annotationType.id).toBeNull();
        expect(annotationType.name).toBeEmptyString();
        expect(annotationType.description).toBeNull();
        expect(annotationType.valueType).toBeEmptyString();
        expect(annotationType.maxValueCount).toBeNull();
        expect(annotationType.options).toBeEmptyArray();

        if (_.contains(objRequiredKeys, 'required')) {
          expect(annotationType.required).toBe(false);
        }
      });

      it('a list can be retrieved from the server', function(done) {
        var serverAnnotType = createServerAnnotTypeFn();
        var objs = [serverAnnotType];

        httpBackend.whenGET(uri(serverAnnotType.studyId)).respond(serverReply(objs));

        annotationTypeListFn(serverAnnotType.studyId).then(function (annotationTypes) {
          expect(annotationTypes).toBeArrayOfSize(objs.length);

          _.each(annotationTypes, function(at) {
            expect(at).toEqual(jasmine.any(AnnotTypeType));
          });
          done();
        });
        httpBackend.flush();
      });

      it('when listing, fails for invalid response from server', function(done) {
        var serverAnnotType = createServerAnnotTypeFn();
        var lastReplyKey = _.last(objRequiredKeys);

        _.each(objRequiredKeys, function(key) {
          var badObjs = [ _.omit(serverAnnotType, key) ];

          httpBackend.whenGET(uri(serverAnnotType.studyId)).respond(serverReply(badObjs));

          annotationTypeListFn(serverAnnotType.studyId).then(function (reply) {
            _.each(reply, function(err) {
              expect(err).toEqual(jasmine.any(Error));
            });

            if (key === lastReplyKey) {
              done();
            }
          });

          httpBackend.flush();
        });
      });

      it('an annotation type can be retrieved from the server', function(done) {
        var serverAnnotType = createServerAnnotTypeFn();

        httpBackend.whenGET(uri(serverAnnotType.studyId) + '?annotationTypeId=' + serverAnnotType.id)
          .respond(serverReply(serverAnnotType));

        annotationTypeGetFn(serverAnnotType.studyId, serverAnnotType.id)
          .then(function (annotationType) {
            expect(annotationType).toEqual(jasmine.any(AnnotTypeType));
            done();
          });
        httpBackend.flush();
      });

      it('when getting, fails for invalid response from server', function(done) {
        var serverAnnotType = createServerAnnotTypeFn();
        var lastReplyKey = _.last(objRequiredKeys);

        _.each(objRequiredKeys, function(key) {
          var badObj = _.omit(serverAnnotType, key);

        httpBackend.whenGET(uri(serverAnnotType.studyId) + '?annotationTypeId=' + serverAnnotType.id)
            .respond(201, serverReply(badObj));

          annotationTypeGetFn(serverAnnotType.studyId, serverAnnotType.id)
            .then(function (err) {
              expect(err).toEqual(jasmine.any(Error));

              if (key === lastReplyKey) {
                done();
              }
            });

          httpBackend.flush();
        });
      });

      it('can be added', function() {
        var baseAnnotType = createServerAnnotTypeFn();
        var annotationType = createAnnotTypeFn(_.omit(baseAnnotType, 'id'));
        var command = addCommand(baseAnnotType);
        var reply = replyAnnotType(baseAnnotType);

        httpBackend.expectPOST(uri(annotationType.studyId), command).respond(201, serverReply(reply));

        annotationType.addOrUpdate().then(function(replyObj) {
          expect(replyObj).toEqual(jasmine.any(AnnotTypeType));
          checkAnnotType(replyObj, reply);
        });
        httpBackend.flush();
      });

      it('can be updated', function() {
        var baseAnnotType = createServerAnnotTypeFn();
        var annotationType = createAnnotTypeFn(baseAnnotType);
        var command = updateCommand(baseAnnotType);
        var reply = replyAnnotType(baseAnnotType);

        httpBackend.expectPUT(uri(annotationType.studyId, annotationType.id), command)
          .respond(201, serverReply(reply));

        annotationType.addOrUpdate().then(function(replyObj) {
          expect(replyObj).toEqual(jasmine.any(AnnotTypeType));
          checkAnnotType(replyObj, reply);
        });
        httpBackend.flush();
      });

      it('can be removed', function() {
        var baseAnnotType = createServerAnnotTypeFn();
        var annotationType = createAnnotTypeFn(baseAnnotType);
        var command = removeCommand(baseAnnotType);

        httpBackend.expectDELETE(uri(annotationType.studyId, annotationType.id, annotationType.version))
          .respond(201);
        annotationType.remove();
        httpBackend.flush();
      });


      it('when adding, fails for invalid response from server', function(done) {
        var baseAnnotType = createServerAnnotTypeFn();
        var command = addCommand(baseAnnotType);
        var reply = replyAnnotType(baseAnnotType);

        checkAddOrUpdateInvalidResponse(uri(baseAnnotType.studyId),
                                        _.omit(baseAnnotType, 'id'),
                                        command,
                                        reply,
                                        'expectPOST',
                                        done);
      });

      it('when updating, fails for invalid response from server', function(done) {
        var baseAnnotType = createServerAnnotTypeFn();
        var command = updateCommand(baseAnnotType);
        var reply = replyAnnotType(baseAnnotType);

        checkAddOrUpdateInvalidResponse(uri(baseAnnotType.studyId, baseAnnotType.id),
                                        baseAnnotType,
                                        command,
                                        reply,
                                        'expectPUT',
                                        done);
      });

      it('values assigned correctly when value type is changed', function() {
        var annotationType = createAnnotTypeFn(createServerAnnotTypeFn());
        annotationType.valueType = AnnotationValueType.TEXT();
        annotationType.valueTypeChanged();
        expect(annotationType.maxValueCount).toBe(null);
        expect(annotationType.options).toBeArrayOfSize(0);

        annotationType.valueType = AnnotationValueType.SELECT();
        annotationType.maxValueCount = AnnotationMaxValueCount.SELECT_SINGLE();
        annotationType.valueTypeChanged();
        expect(annotationType.maxValueCount).toBe(AnnotationMaxValueCount.SELECT_SINGLE());
        expect(annotationType.options).toBeArrayOfSize(0);

        annotationType.options.push('test');
        annotationType.valueType = AnnotationValueType.NUMBER();
        annotationType.valueTypeChanged();
        expect(annotationType.maxValueCount).toBe(null);
        expect(annotationType.options).toBeArrayOfSize(0);
      });


      function uri(/* studyId, annotationTypeId, version */) {
        var args = _.toArray(arguments);
        var studyId, annotationTypeId, version, result;

        if (args.length <= 0) {
          throw new Error('study id specified');
        }

        studyId = args.shift();
        result = '/studies/' + studyId + annotationTypeUriPart;

        if (args.length > 0) {
          annotationTypeId = args.shift();
          result += '/' + annotationTypeId;
        }
        if (args.length > 0) {
          version = args.shift();
          result += '/' + version;
        }
        return result;
      }

      function addCommand(annotationType) {
        return _.extend(
          _.pick(annotationType, 'studyId', 'name', 'valueType', 'options', 'required'),
          funutils.pickOptional(annotationType, 'description', 'maxValueCount'));
      }

      function updateCommand(annotationType) {
        return _.extend(addCommand(annotationType),
                        { id: annotationType.id, expectedVersion: annotationType.version });
      }

      function removeCommand(annotationType) {
        return _.extend(_.pick(annotationType, 'id', 'studyId'),
                        { expectedVersion: annotationType.version });
      }

      function replyAnnotType(annotationType, newValues) {
        newValues = newValues || {};
        return createAnnotTypeFn(_.extend({},
                                          annotationType,
                                          newValues,
                                          {version: annotationType.version + 1}));
      }

      function serverReply(obj) {
        return { status: 'success', data: obj };
      }

      function checkAddOrUpdateInvalidResponse(uri,
                                               serverAnnotType,
                                               command,
                                               replyAnnotType,
                                               httpBackendMethod,
                                               done) {
        var lastReplyKey = _.last(objRequiredKeys);

        _.each(objRequiredKeys, function(key) {
          var annotationType = createAnnotTypeFn(serverAnnotType);
          var replyBadAnnotType = _.omit(replyAnnotType, key);

          httpBackend[httpBackendMethod](uri, command).respond(201, serverReply(replyBadAnnotType));

          annotationType.addOrUpdate().then(function (reply) {
            expect(reply).toEqual(jasmine.any(Error));

            if (key === lastReplyKey) {
              done();
            }
          });
        });

        httpBackend.flush();
      }

      function checkAnnotType(newObj, orig) {
        expect(newObj).toHaveNonEmptyString('id');
        expect(newObj.name).toEqual(orig.name);
        expect(newObj.description).toEqual(orig.description);
        expect(newObj.valueType).toEqual(orig.valueType);
        expect(newObj.options).toEqual(orig.options);
      }

    });

  }

  return studyAnnotationTypeSharedSpec;

});


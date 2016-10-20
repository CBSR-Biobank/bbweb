/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  /**
   * Shared spec for all 3 services listed above.
   */
  function studyAnnotationTypeSharedSpec(context) {

    describe('(shared)', function() {

      var httpBackend,
          funutils,
          AnnotationTypeType,
          AnnotationValueType,
          AnnotationMaxValueCount,
          createAnnotationTypeFn,
          annotationTypesService,
          annotationTypeUriPart,
          objRequiredKeys,
          createServerAnnotationTypeFn,
          annotationTypeListFn,
          annotationTypeGetFn;

      beforeEach(inject(function($httpBackend,
                                 _funutils_,
                                 _AnnotationValueType_,
                                 _AnnotationMaxValueCount_,
                                ServerReplyMixin) {
        _.extend(this, ServerReplyMixin.prototype);
        httpBackend                  = $httpBackend;
        funutils                     = _funutils_;
        AnnotationValueType          = _AnnotationValueType_;
        AnnotationMaxValueCount      = _AnnotationMaxValueCount_;
        AnnotationTypeType           = context.annotationTypeType;
        createAnnotationTypeFn       = context.createAnnotationTypeFn;
        annotationTypesService       = context.annotationTypesService;
        annotationTypeUriPart        = context.annotationTypeUriPart;
        objRequiredKeys              = context.objRequiredKeys;
        createServerAnnotationTypeFn = context.createServerAnnotationTypeFn;
        annotationTypeListFn         = context.annotationTypeListFn;
        annotationTypeGetFn          = context.annotationTypeGetFn;
      }));

      it('has default values', function() {
        var annotationType = new AnnotationTypeType();

        expect(annotationType.id).toBeNull();
        expect(annotationType.name).toBeEmptyString();
        expect(annotationType.description).toBeNull();
        expect(annotationType.valueType).toBeEmptyString();
        expect(annotationType.maxValueCount).toBeNull();
        expect(annotationType.options).toBeEmptyArray();

        if (_.includes(objRequiredKeys, 'required')) {
          expect(annotationType.required).toBe(false);
        }
      });

      it('a list can be retrieved from the server', function(done) {
        var serverAnnotationType = createServerAnnotationTypeFn();
        var objs = [serverAnnotationType];

        httpBackend.whenGET(uri(serverAnnotationType.studyId)).respond(this.reply(objs));

        annotationTypeListFn(serverAnnotationType.studyId).then(function (annotationTypes) {
          expect(annotationTypes).toBeArrayOfSize(objs.length);

          _.each(annotationTypes, function(at) {
            expect(at).toEqual(jasmine.any(AnnotationTypeType));
          });
          done();
        });
        httpBackend.flush();
      });

      it('when listing, fails for invalid response from server', function(done) {
        var serverAnnotationType = createServerAnnotationTypeFn();
        var lastReplyKey = _.last(objRequiredKeys);

        _.each(objRequiredKeys, function(key) {
          var badObjs = [ _.omit(serverAnnotationType, key) ];

          httpBackend.whenGET(uri(serverAnnotationType.studyId)).respond(this.reply(badObjs));

          annotationTypeListFn(serverAnnotationType.studyId).then(function (reply) {
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
        var serverAnnotationType = createServerAnnotationTypeFn();

        httpBackend.whenGET(uri(serverAnnotationType.studyId) + '?annotTypeId=' + serverAnnotationType.id)
          .respond(this.reply(serverAnnotationType));

        annotationTypeGetFn(serverAnnotationType.studyId, serverAnnotationType.id)
          .then(function (annotationType) {
            expect(annotationType).toEqual(jasmine.any(AnnotationTypeType));
            done();
          });
        httpBackend.flush();
      });

      it('when getting, fails for invalid response from server', function(done) {
        var serverAnnotationType = createServerAnnotationTypeFn();
        var lastReplyKey = _.last(objRequiredKeys);

        _.each(objRequiredKeys, function(key) {
          var badObj = _.omit(serverAnnotationType, key);

        httpBackend.whenGET(uri(serverAnnotationType.studyId) + '?annotTypeId=' + serverAnnotationType.id)
            .respond(this.reply(badObj));

          annotationTypeGetFn(serverAnnotationType.studyId, serverAnnotationType.id)
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
        var baseAnnotationType = createServerAnnotationTypeFn();
        var annotationType = createAnnotationTypeFn(_.omit(baseAnnotationType, 'id'));
        var command = addCommand(baseAnnotationType);
        var reply = replyAnnotationType(baseAnnotationType);

        httpBackend.expectPOST(uri(annotationType.studyId), command).respond(this.reply(reply));

        annotationType.addOrUpdate().then(function(replyObj) {
          expect(replyObj).toEqual(jasmine.any(AnnotationTypeType));
          checkAnnotationType(replyObj, reply);
        });
        httpBackend.flush();
      });

      it('can be updated', function() {
        var baseAnnotationType = createServerAnnotationTypeFn();
        var annotationType = createAnnotationTypeFn(baseAnnotationType);
        var command = updateCommand(baseAnnotationType);
        var reply = replyAnnotationType(baseAnnotationType);

        httpBackend.expectPUT(uri(annotationType.studyId, annotationType.id), command)
          .respond(this.reply(reply));

        annotationType.addOrUpdate().then(function(replyObj) {
          expect(replyObj).toEqual(jasmine.any(AnnotationTypeType));
          checkAnnotationType(replyObj, reply);
        });
        httpBackend.flush();
      });

      it('can be removed', function() {
        var baseAnnotationType = createServerAnnotationTypeFn();
        var annotationType = createAnnotationTypeFn(baseAnnotationType);

        httpBackend.expectDELETE(uri(annotationType.studyId, annotationType.id, annotationType.version))
          .respond(201);
        annotationType.remove();
        httpBackend.flush();
      });


      it('when adding, fails for invalid response from server', function(done) {
        var baseAnnotationType = createServerAnnotationTypeFn();
        var command = addCommand(baseAnnotationType);
        var reply = replyAnnotationType(baseAnnotationType);

        checkAddOrUpdateInvalidResponse(uri(baseAnnotationType.studyId),
                                        _.omit(baseAnnotationType, 'id'),
                                        command,
                                        reply,
                                        'expectPOST',
                                        done);
      });

      it('when updating, fails for invalid response from server', function(done) {
        var baseAnnotationType = createServerAnnotationTypeFn();
        var command = updateCommand(baseAnnotationType);
        var reply = replyAnnotationType(baseAnnotationType);

        checkAddOrUpdateInvalidResponse(uri(baseAnnotationType.studyId, baseAnnotationType.id),
                                        baseAnnotationType,
                                        command,
                                        reply,
                                        'expectPUT',
                                        done);
      });

      it('values assigned correctly when value type is changed', function() {
        var annotationType = createAnnotationTypeFn(createServerAnnotationTypeFn());
        annotationType.valueType = AnnotationValueType.TEXT;
        annotationType.valueTypeChanged();
        expect(annotationType.maxValueCount).toBe(null);
        expect(annotationType.options).toBeArrayOfSize(0);

        annotationType.valueType = AnnotationValueType.SELECT;
        annotationType.maxValueCount = AnnotationMaxValueCount.SELECT_SINGLE;
        annotationType.valueTypeChanged();
        expect(annotationType.maxValueCount).toBe(AnnotationMaxValueCount.SELECT_SINGLE);
        expect(annotationType.options).toBeArrayOfSize(0);

        annotationType.options.push('test');
        annotationType.valueType = AnnotationValueType.NUMBER;
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

      function replyAnnotationType(annotationType, newValues) {
        newValues = newValues || {};
        return createAnnotationTypeFn(_.extend({},
                                          annotationType,
                                          newValues,
                                          {version: annotationType.version + 1}));
      }

      function checkAddOrUpdateInvalidResponse(uri,
                                               serverAnnotationType,
                                               command,
                                               replyAnnotationType,
                                               httpBackendMethod,
                                               done) {
        var lastReplyKey = _.last(objRequiredKeys);

        _.each(objRequiredKeys, function(key) {
          var annotationType = createAnnotationTypeFn(serverAnnotationType);
          var replyBadAnnotationType = _.omit(replyAnnotationType, key);

          httpBackend[httpBackendMethod](uri, command).respond(this.reply(replyBadAnnotationType));

          annotationType.addOrUpdate().then(function (reply) {
            expect(reply).toEqual(jasmine.any(Error));

            if (key === lastReplyKey) {
              done();
            }
          });
        });

        httpBackend.flush();
      }

      function checkAnnotationType(newObj, orig) {
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

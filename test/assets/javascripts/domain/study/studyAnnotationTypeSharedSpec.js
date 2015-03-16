/**
 * Jasmine test suite
 */
define(['underscore', 'biobank.testUtils'], function(_, testUtils) {
  'use strict';

  /**
   * Shared spec for all 3 services listed above.
   */
  function studyAnnotationTypeSharedSpec(context) {

    describe('(shared)', function() {

      var httpBackend, funutils, AnnotTypeType, createAnnotTypeFn, annotTypesService, annotTypeUriPart;
      var objRequiredKeys, createServerAnnotTypeFn, annotTypeListFn, annotTypeGetFn;

      beforeEach(inject(function($httpBackend, _funutils_) {
        testUtils.addCustomMatchers();

        httpBackend              = $httpBackend;
        funutils                 = _funutils_;
        AnnotTypeType            = context.annotTypeType;
        createAnnotTypeFn        = context.createAnnotTypeFn;
        annotTypesService        = context.annotTypesService;
        annotTypeUriPart         = context.annotTypeUriPart;
        objRequiredKeys          = context.objRequiredKeys;
        createServerAnnotTypeFn  = context.createServerAnnotTypeFn;
        annotTypeListFn          = context.annotTypeListFn;
        annotTypeGetFn           = context.annotTypeGetFn;
      }));

      it('a list can be retrieved from the server', function(done) {
        var serverAnnotType = createServerAnnotTypeFn();
        var objs = [serverAnnotType];

        httpBackend.whenGET(uri(serverAnnotType.studyId)).respond(serverReply(objs));

        annotTypeListFn(serverAnnotType.studyId).then(function (annotTypes) {
          expect(annotTypes).toBeArrayOfSize(objs.length);

          _.each(annotTypes, function(at) {
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

          annotTypeListFn(serverAnnotType.studyId).then(function (reply) {
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

        httpBackend.whenGET(uri(serverAnnotType.studyId, serverAnnotType.id))
          .respond(serverReply(serverAnnotType));

        annotTypeGetFn(serverAnnotType.studyId, serverAnnotType.id)
          .then(function (annotType) {
            expect(annotType).toEqual(jasmine.any(AnnotTypeType));
            done();
          });
        httpBackend.flush();
      });

      it('when getting, fails for invalid response from server', function(done) {
        var serverAnnotType = createServerAnnotTypeFn();
        var lastReplyKey = _.last(objRequiredKeys);

        _.each(objRequiredKeys, function(key) {
          var badObj = _.omit(serverAnnotType, key);

          httpBackend.whenGET(uri(serverAnnotType.studyId, serverAnnotType.id))
            .respond(201, serverReply(badObj));

          annotTypeGetFn(serverAnnotType.studyId, serverAnnotType.id)
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
        var annotType = createAnnotTypeFn(_.omit(baseAnnotType, 'id'));
        var command = addCommand(baseAnnotType);
        var reply = replyAnnotType(baseAnnotType);

        httpBackend.expectPOST(uri(annotType.studyId), command).respond(201, serverReply(reply));

        annotType.addOrUpdate().then(function(replyObj) {
          expect(replyObj).toEqual(jasmine.any(AnnotTypeType));
          checkAnnotType(replyObj, reply);
        });
        httpBackend.flush();
      });

      it('can be updated', function() {
        var baseAnnotType = createServerAnnotTypeFn();
        var annotType = createAnnotTypeFn(baseAnnotType);
        var command = updateCommand(baseAnnotType);
        var reply = replyAnnotType(baseAnnotType);

        httpBackend.expectPUT(uri(annotType.studyId, annotType.id), command)
          .respond(201, serverReply(reply));

        annotType.addOrUpdate().then(function(replyObj) {
          expect(replyObj).toEqual(jasmine.any(AnnotTypeType));
          checkAnnotType(replyObj, reply);
        });
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
                                        httpBackend.expectPOST,
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
                                        httpBackend.expectPUT,
                                        done);
      });

      function uri(/* studyId, annotTypeId, version */) {
        var args = _.toArray(arguments);
        var studyId, annotTypeId, version, result;

        if (args.length <= 0) {
          throw new Error('study id specified');
        }

        studyId = args.shift();
        result = '/studies/' + studyId + annotTypeUriPart;

        if (args.length > 0) {
          annotTypeId = args.shift();
          result += '/' + annotTypeId;
        }
        if (args.length > 0) {
          version = args.shift();
          result += '/' + version;
        }
        return result;
      }

      function addCommand(annotType) {
        return _.extend(
          _.pick(annotType, 'studyId', 'name', 'valueType', 'options', 'required'),
          funutils.pickOptional(annotType, 'description', 'maxValueCount'));
      }

      function updateCommand(annotType) {
        return _.extend(addCommand(annotType),
                        { id: annotType.id, expectedVersion: annotType.version });
      }

      function replyAnnotType(annotType, newValues) {
        newValues = newValues || {};
        return createAnnotTypeFn(_.extend({},
                                          annotType,
                                          newValues,
                                          {version: annotType.version + 1}));
      }

      function serverReply(obj) {
        return { status: 'success', data: obj };
      }

      function checkAddOrUpdateInvalidResponse(replyRequiredKeys,
                                               uri,
                                               serverAnnotType,
                                               command,
                                               replyAnnotType,
                                               httpBackendExpectFn,
                                               done) {
        var lastReplyKey = _.last(replyRequiredKeys);

        _.each(objRequiredKeys, function(key) {
          var annotType = createAnnotTypeFn(serverAnnotType);
          var replyBadAnnotType = _.omit(replyAnnotType, key);

          httpBackendExpectFn(uri, command).respond(201, serverReply(replyBadAnnotType));

          console.log(replyBadAnnotType);

          annotType.addOrUpdate().then(function (reply) {
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


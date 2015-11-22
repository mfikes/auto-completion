//
//  FCCHttpClient.m
//  Classroom Checkout
//
//  Created by Mike Fikes on 1/1/15.
//  Copyright (c) 2015 FikesFarm. All rights reserved.
//

#import "FCJHttpClient.h"
#import "AFNetworking.h"

@interface FCJHttpClient()

@property (strong, nonatomic) AFHTTPRequestOperationManager *manager;

@end

@implementation FCJHttpClient

+ (FCJHttpClient*)create
{
    return [[FCJHttpClient alloc] init];
}

-(id)init {
    if (self = [super init]) {
        self.manager = [AFHTTPRequestOperationManager manager];
    }
    
    return self;
}

- (NSDictionary*)lowercaseKeysFor:(NSDictionary*)dict
{
    NSMutableDictionary* rv = [[NSMutableDictionary alloc] init];
    
    for (NSString* key in dict.allKeys) {
        [rv setObject:[dict objectForKey:key] forKey:[key lowercaseString]];
    }
    
    return rv;
}

- (void)requestMethod:(NSString*)method url:(NSString*)url parameters:(NSDictionary*)parameters callback:(JSValue *)callback
{    
    id successBlock;
    
    if ([method isEqualToString:@"head"]) {
        successBlock = ^(AFHTTPRequestOperation *operation) {
            [callback callWithArguments:@[@{@"status": @(operation.response.statusCode),
                                            @"body": operation.responseString,
                                            @"headers": [self lowercaseKeysFor:operation.response.allHeaderFields]}]];
        };
    } else {
        successBlock = ^(AFHTTPRequestOperation *operation, id responseObject) {
            [callback callWithArguments:@[@{@"status": @(operation.response.statusCode),
                                            @"body": operation.responseString,
                                            @"headers": [self lowercaseKeysFor:operation.response.allHeaderFields],
                                            @"response-object": responseObject}]];
        };
    }
    
    id failureBlock = ^(AFHTTPRequestOperation *operation, NSError *error) {
        if (operation.response) {
            [callback callWithArguments:@[@{@"status": @(operation.response.statusCode),
                                            @"body": operation.responseString,
                                            @"headers": [self lowercaseKeysFor:operation.response.allHeaderFields]}]];
        } else {
            [callback callWithArguments:@[@{@"error": [error localizedDescription]}]];
        }
    };
    
    if ([method isEqualToString:@"get"]) {
        [self.manager GET:url parameters:parameters success:successBlock failure:failureBlock];
    } else if ([method isEqualToString:@"post"]) {
        
        if (parameters[@"multipart"]) {
            
            NSMutableDictionary* requestParameters = [[NSMutableDictionary alloc] init];
            NSMutableArray* fileParts = [[NSMutableArray alloc] init];
            
            
            for (NSDictionary* part in parameters[@"multipart"]) {
                if ([part objectForKey:@"filename"]) {
                    [fileParts addObject:part];
                } else {
                    [requestParameters setObject:part[@"content"] forKey: part[@"name"]];
                }
            }
            
            [self.manager                POST:url
                                   parameters:requestParameters
                    constructingBodyWithBlock:^(id<AFMultipartFormData> formData) {
                        for (NSDictionary* filePart in fileParts) {
                            [formData appendPartWithFileData:filePart[@"content"]
                                                        name:filePart[@"name"]
                                                    fileName:filePart[@"filename"]
                                                    mimeType:@"application/octetstream"];
                        }
                    }
                                      success:successBlock
                                      failure:failureBlock];
            
        } else {
            [self.manager POST:url parameters:parameters success:successBlock failure:failureBlock];
        }
    } else if ([method isEqualToString:@"put"]) {
        [self.manager PUT:url parameters:parameters success:successBlock failure:failureBlock];
    } else if ([method isEqualToString:@"patch"]) {
        [self.manager PATCH:url parameters:parameters success:successBlock failure:failureBlock];
    } else if ([method isEqualToString:@"delete"]) {
        [self.manager DELETE:url parameters:parameters success:successBlock failure:failureBlock];
    } else if ([method isEqualToString:@"post"]) {
        [self.manager POST:url parameters:parameters success:successBlock failure:failureBlock];
    }
    
    
}

@end

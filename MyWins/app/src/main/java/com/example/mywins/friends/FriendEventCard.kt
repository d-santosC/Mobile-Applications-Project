package com.example.mywins.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.mywins.database.AppDatabase
import com.example.mywins.database.user.User
import com.example.mywins.database.activityEvent.ActivityEvent
import com.example.mywins.database.comment.Comment
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import coil.compose.rememberAsyncImagePainter
import com.example.mywins.database.like.Like
import java.io.File


@Composable
fun FriendEventCard(event: ActivityEvent, user: User, currentUserEmail: String,  userMap: Map<String, User>, onEventDeleted: (Int) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getInstance(context)

    var likeCount by remember { mutableIntStateOf(0) }
    var userLiked by remember { mutableStateOf(false) }
    var comments by remember { mutableStateOf(emptyList<Comment>()) }
    var commentText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(event.id) {
        scope.launch {
            likeCount = db.likeDao().getLikeCount(event.id)
            userLiked = db.likeDao().hasUserLiked(event.id, currentUserEmail) != null
            comments = db.commentDao().getComments(event.id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEFE3FF), shape = RoundedCornerShape(12.dp))
            .padding(16.dp)

    ) {

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (event.userEmail == currentUserEmail) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = Color.Black
                    )
                }
            }
        }

        val profilePictureFile = remember(user.profilePictureUri) {
            user.profilePictureUri
                ?.takeIf { it.isNotBlank() && it != "null" }
                ?.let { path ->
                    val file = File(path)
                    if (file.exists()) file else null
                }
        }

        if (profilePictureFile != null) {
            Image(
                painter = rememberAsyncImagePainter(profilePictureFile),
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
        } else {
            Icon(
                Icons.Default.Person,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
        }

        Text("${user.name} ${if (event.type == "created") "created" else "completed"} '${event.goalName}'", fontWeight = FontWeight.Bold)

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                scope.launch {
                    if (userLiked) {
                        // Se já deu like, remove
                        db.likeDao().removeLike(event.id, currentUserEmail)
                        likeCount--
                        userLiked = false
                    } else {
                        // Se ainda não deu like, adiciona
                        db.likeDao().addLike(Like(eventId = event.id, userEmail = currentUserEmail))
                        likeCount++
                        userLiked = true
                    }
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Like",
                    tint = if (userLiked) Color.Red else Color.Gray
                )
            }
            Text("$likeCount likes")
        }

        Spacer(Modifier.height(8.dp))
        comments.forEach {
            val authorName = userMap[it.userEmail]?.name ?: it.userEmail
            Row {
                Text(
                    text = "$authorName: ",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF9F7AEA),
                    fontSize = 14.sp
                )
                Text(
                    text = it.text,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            placeholder = { Text("Write Comment...") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(2.dp))

        Button(
            onClick = {
                scope.launch {
                    if (commentText.isNotBlank()) {
                        db.commentDao().addComment(
                            Comment(
                                eventId = event.id,
                                userEmail = currentUserEmail,
                                text = commentText,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        comments = db.commentDao().getComments(event.id)
                        commentText = ""
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9F7AEA))


        ) {
            Text("Comment")
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Do you really wish to eliminate this post?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        db.commentDao().deleteCommentsByEventId(event.id)
                        db.likeDao().deleteLikesByEventId(event.id)
                        db.activityEventDao().deleteEventById(event.id)
                        onEventDeleted(event.id)
                        showDeleteDialog = false
                    }
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("No")
                }
            }
        )
    }


}